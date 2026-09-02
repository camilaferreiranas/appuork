data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  ami_id = var.ec2_ami_id != "" ? var.ec2_ami_id : nonsensitive(data.aws_ssm_parameter.al2023.value)

  user_data = base64encode(templatefile("${path.module}/templates/user_data.sh.tftpl", {
    aws_region      = var.aws_region
    db_secret_arn   = aws_secretsmanager_secret.db.arn
    app_secret_arn  = aws_secretsmanager_secret.app.arn
    app_port        = var.app_port
    artifact_s3_uri = var.artifact_s3_uri
    java_package    = var.java_package
    app_bucket      = aws_s3_bucket.app.bucket
  }))
}

resource "aws_instance" "app" {
  ami           = local.ami_id
  instance_type = var.ec2_instance_type

  subnet_id              = aws_subnet.private[0].id
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.app.name

  user_data_base64            = local.user_data
  user_data_replace_on_change = true
  monitoring                  = true

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required" # IMDSv2 obrigatorio
    http_put_response_hop_limit = 2
  }

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.ec2_root_volume_size
    encrypted             = true
    delete_on_termination = true
  }

  depends_on = [
    aws_secretsmanager_secret_version.db,
    aws_secretsmanager_secret_version.app,
    aws_nat_gateway.this,
  ]

  tags = merge(local.common_tags, { Name = "${local.name}-app" })

  lifecycle {
    ignore_changes = [ami]
  }
}
