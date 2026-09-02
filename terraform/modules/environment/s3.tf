data "aws_caller_identity" "current" {}

locals {
  app_bucket_name  = "${local.name}-app-${data.aws_caller_identity.current.account_id}"
  logs_bucket_name = "${local.name}-logs-${data.aws_caller_identity.current.account_id}"
}

# ===========================================================================
# Bucket de logs de acesso (destino do server access logging do bucket da app)
# ===========================================================================
resource "aws_s3_bucket" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket        = local.logs_bucket_name
  force_destroy = var.app_bucket_force_destroy

  tags = merge(local.common_tags, { Name = local.logs_bucket_name })
}

resource "aws_s3_bucket_public_access_block" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket                  = aws_s3_bucket.logs[0].id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket = aws_s3_bucket.logs[0].id
  rule {
    # Log delivery moderno usa a bucket policy (service principal), sem ACL
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket = aws_s3_bucket.logs[0].id
  rule {
    apply_server_side_encryption_by_default {
      # Log delivery exige SSE-S3 (AES256)
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket = aws_s3_bucket.logs[0].id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket = aws_s3_bucket.logs[0].id

  rule {
    id     = "expira-logs"
    status = "Enabled"

    filter {}

    expiration {
      days = var.s3_logs_expiration_days
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

data "aws_iam_policy_document" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  # Somente TLS
  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["s3:*"]
    resources = [aws_s3_bucket.logs[0].arn, "${aws_s3_bucket.logs[0].arn}/*"]
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }

  # Permite o servico de server access logging gravar
  statement {
    sid       = "S3ServerAccessLogsWrite"
    effect    = "Allow"
    actions   = ["s3:PutObject"]
    resources = ["${aws_s3_bucket.logs[0].arn}/s3/*"]
    principals {
      type        = "Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
    condition {
      test     = "ArnLike"
      variable = "aws:SourceArn"
      values   = [aws_s3_bucket.app.arn]
    }
    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_s3_bucket_policy" "logs" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket = aws_s3_bucket.logs[0].id
  policy = data.aws_iam_policy_document.logs[0].json

  depends_on = [aws_s3_bucket_public_access_block.logs]
}

# ===========================================================================
# Bucket da aplicacao (uploads, anexos, etc.)
# ===========================================================================
resource "aws_s3_bucket" "app" {
  bucket        = local.app_bucket_name
  force_destroy = var.app_bucket_force_destroy

  tags = merge(local.common_tags, { Name = local.app_bucket_name })
}

# Bloqueia qualquer acesso publico
resource "aws_s3_bucket_public_access_block" "app" {
  bucket                  = aws_s3_bucket.app.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Desabilita ACLs - o dono da conta e sempre o dono dos objetos
resource "aws_s3_bucket_ownership_controls" "app" {
  bucket = aws_s3_bucket.app.id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

# Criptografia em repouso com a KMS dedicada do ambiente
resource "aws_s3_bucket_server_side_encryption_configuration" "app" {
  bucket = aws_s3_bucket.app.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.main.arn
    }
    bucket_key_enabled = true
  }
}

# Versionamento (protege contra sobrescrita/exclusao acidental)
resource "aws_s3_bucket_versioning" "app" {
  bucket = aws_s3_bucket.app.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Ciclo de vida: limpa versoes antigas e uploads multipart incompletos
resource "aws_s3_bucket_lifecycle_configuration" "app" {
  bucket = aws_s3_bucket.app.id

  rule {
    id     = "higiene"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = var.s3_noncurrent_version_expiration_days
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# Envia logs de acesso para o bucket de logs
resource "aws_s3_bucket_logging" "app" {
  count = var.enable_s3_access_logging ? 1 : 0

  bucket        = aws_s3_bucket.app.id
  target_bucket = aws_s3_bucket.logs[0].id
  target_prefix = "s3/${local.app_bucket_name}/"

  depends_on = [aws_s3_bucket_policy.logs]
}

# Politica: nega trafego sem TLS e uploads sem a criptografia KMS correta
data "aws_iam_policy_document" "app_bucket" {
  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["s3:*"]
    resources = [aws_s3_bucket.app.arn, "${aws_s3_bucket.app.arn}/*"]
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }

  statement {
    sid       = "DenyWrongKmsKey"
    effect    = "Deny"
    actions   = ["s3:PutObject"]
    resources = ["${aws_s3_bucket.app.arn}/*"]
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }
    condition {
      test     = "StringNotEquals"
      variable = "s3:x-amz-server-side-encryption-aws-kms-key-id"
      values   = [aws_kms_key.main.arn]
    }
    condition {
      test     = "Null"
      variable = "s3:x-amz-server-side-encryption-aws-kms-key-id"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "app" {
  bucket = aws_s3_bucket.app.id
  policy = data.aws_iam_policy_document.app_bucket.json

  depends_on = [aws_s3_bucket_public_access_block.app]
}

# ===========================================================================
# Permissao da role da EC2 para usar o bucket da aplicacao
# ===========================================================================
data "aws_iam_policy_document" "app_s3" {
  statement {
    sid       = "ListAppBucket"
    effect    = "Allow"
    actions   = ["s3:ListBucket", "s3:GetBucketLocation"]
    resources = [aws_s3_bucket.app.arn]
  }

  statement {
    sid    = "ReadWriteAppObjects"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
    ]
    resources = ["${aws_s3_bucket.app.arn}/*"]
  }

  # A KMS dedicada ja permite Decrypt na iam.tf; aqui adiciona o necessario p/ upload
  statement {
    sid    = "UseKmsForS3"
    effect = "Allow"
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:GenerateDataKey",
    ]
    resources = [aws_kms_key.main.arn]
  }
}

resource "aws_iam_role_policy" "app_s3" {
  name   = "${local.name}-app-s3"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app_s3.json
}
