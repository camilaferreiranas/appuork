locals {
  # Extrai o nome do bucket a partir de artifact_s3_uri (s3://bucket/chave...)
  artifact_bucket = var.artifact_s3_uri != "" ? split("/", replace(var.artifact_s3_uri, "s3://", ""))[0] : ""
}

# ---------------------------------------------------------------------------
# Role da instancia EC2 (backend)
# ---------------------------------------------------------------------------
data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "app" {
  name               = "${local.name}-app-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
  tags               = local.common_tags
}

# Leitura dos secrets especificos deste ambiente + decrypt com a KMS dedicada
data "aws_iam_policy_document" "app_secrets" {
  statement {
    sid    = "ReadEnvSecrets"
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
    ]
    resources = [
      aws_secretsmanager_secret.db.arn,
      aws_secretsmanager_secret.app.arn,
    ]
  }

  statement {
    sid       = "DecryptWithEnvKey"
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = [aws_kms_key.main.arn]
  }
}

resource "aws_iam_role_policy" "app_secrets" {
  name   = "${local.name}-app-secrets"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app_secrets.json
}

# Acesso somente-leitura ao artefato no S3 (quando configurado)
data "aws_iam_policy_document" "app_artifact" {
  count = local.artifact_bucket != "" ? 1 : 0

  statement {
    sid       = "GetArtifact"
    effect    = "Allow"
    actions   = ["s3:GetObject"]
    resources = ["arn:aws:s3:::${local.artifact_bucket}/*"]
  }

  statement {
    sid       = "ListArtifactBucket"
    effect    = "Allow"
    actions   = ["s3:ListBucket"]
    resources = ["arn:aws:s3:::${local.artifact_bucket}"]
  }
}

resource "aws_iam_role_policy" "app_artifact" {
  count  = local.artifact_bucket != "" ? 1 : 0
  name   = "${local.name}-app-artifact"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app_artifact[0].json
}

# SSM Session Manager (acesso ao shell sem SSH/chave/bastion) + CloudWatch agent
resource "aws_iam_role_policy_attachment" "app_ssm" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "app_cw" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_instance_profile" "app" {
  name = "${local.name}-app-profile"
  role = aws_iam_role.app.name
  tags = local.common_tags
}

# ---------------------------------------------------------------------------
# Role para o Enhanced Monitoring do RDS
# ---------------------------------------------------------------------------
data "aws_iam_policy_document" "rds_monitoring_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["monitoring.rds.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "rds_monitoring" {
  name               = "${local.name}-rds-monitoring"
  assume_role_policy = data.aws_iam_policy_document.rds_monitoring_assume.json
  tags               = local.common_tags
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  role       = aws_iam_role.rds_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}
