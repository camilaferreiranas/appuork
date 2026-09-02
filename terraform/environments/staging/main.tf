terraform {
  required_version = ">= 1.5"

  # Backend remoto. Rode primeiro o modulo ../../bootstrap e depois:
  #   terraform init -backend-config=backend.hcl
  backend "s3" {
    key = "staging/terraform.tfstate"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "appuork"
      Environment = "staging"
      ManagedBy   = "terraform"
    }
  }
}

module "stack" {
  source = "../../modules/environment"

  project_name = "appuork"
  environment  = "staging"
  aws_region   = var.aws_region

  # Rede
  vpc_cidr             = "10.20.0.0/16"
  availability_zones   = ["${var.aws_region}a", "${var.aws_region}b"]
  public_subnet_cidrs  = ["10.20.0.0/24", "10.20.1.0/24"]
  private_subnet_cidrs = ["10.20.10.0/24", "10.20.11.0/24"]

  # Banco - dimensionamento enxuto para staging
  db_instance_class          = "db.t4g.micro"
  db_allocated_storage       = 20
  db_max_allocated_storage   = 100
  db_multi_az                = false
  db_backup_retention_period = 7
  db_deletion_protection     = false

  # Backend
  ec2_instance_type = "t3.small"
  app_port          = 8080
  health_check_path = "/actuator/health"
  artifact_s3_uri   = var.artifact_s3_uri

  # S3 - em staging permite destruir o bucket com objetos
  app_bucket_force_destroy              = true
  s3_noncurrent_version_expiration_days = 30

  # TLS (defina certificate_arn e mude para true quando tiver dominio/ACM)
  enable_https       = var.enable_https
  certificate_arn    = var.certificate_arn
  allowed_http_cidrs = var.allowed_http_cidrs

  # Variaveis de ambiente extras - semeadas uma vez no Secrets Manager
  app_environment_secrets = var.app_environment_secrets
}
