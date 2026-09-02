terraform {
  required_version = ">= 1.5"

  # Backend remoto. Rode primeiro o modulo ../../bootstrap e depois:
  #   terraform init -backend-config=backend.hcl
  backend "s3" {
    key = "prod/terraform.tfstate"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "appuork"
      Environment = "prod"
      ManagedBy   = "terraform"
    }
  }
}

module "stack" {
  source = "../../modules/environment"

  project_name = "appuork"
  environment  = "prod"
  aws_region   = var.aws_region

  # Rede - CIDR distinto do staging (facilita peering/auditoria)
  vpc_cidr             = "10.30.0.0/16"
  availability_zones   = ["${var.aws_region}a", "${var.aws_region}b"]
  public_subnet_cidrs  = ["10.30.0.0/24", "10.30.1.0/24"]
  private_subnet_cidrs = ["10.30.10.0/24", "10.30.11.0/24"]

  # Banco - HA e protecoes de producao
  db_instance_class          = "db.t4g.medium"
  db_allocated_storage       = 50
  db_max_allocated_storage   = 500
  db_multi_az                = true
  db_backup_retention_period = 30
  db_deletion_protection     = true

  # Backend
  ec2_instance_type = "t3.medium"
  app_port          = 8080
  health_check_path = "/actuator/health"
  artifact_s3_uri   = var.artifact_s3_uri

  # S3 - em prod o bucket e protegido contra destruicao acidental
  app_bucket_force_destroy              = false
  s3_noncurrent_version_expiration_days = 180

  # TLS obrigatorio em producao
  enable_https       = var.enable_https
  certificate_arn    = var.certificate_arn
  allowed_http_cidrs = var.allowed_http_cidrs

  app_environment_secrets = var.app_environment_secrets
}
