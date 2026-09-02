variable "project_name" {
  description = "Nome do projeto, usado como prefixo dos recursos."
  type        = string
  default     = "appuork"
}

variable "environment" {
  description = "Nome do ambiente (ex.: staging, prod)."
  type        = string

  validation {
    condition     = contains(["staging", "prod"], var.environment)
    error_message = "environment deve ser 'staging' ou 'prod'."
  }
}

variable "aws_region" {
  description = "Regiao AWS onde os recursos serao criados."
  type        = string
  default     = "us-east-1"
}

# ---------------------------------------------------------------------------
# Rede
# ---------------------------------------------------------------------------
variable "vpc_cidr" {
  description = "Bloco CIDR da VPC."
  type        = string
}

variable "availability_zones" {
  description = "Lista de AZs (minimo 2) para alta disponibilidade."
  type        = list(string)

  validation {
    condition     = length(var.availability_zones) >= 2
    error_message = "Informe pelo menos 2 availability zones."
  }
}

variable "public_subnet_cidrs" {
  description = "CIDRs das subnets publicas (ALB / NAT). Uma por AZ."
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "CIDRs das subnets privadas (EC2 / RDS). Uma por AZ."
  type        = list(string)
}

# ---------------------------------------------------------------------------
# Banco de dados (RDS PostgreSQL)
# ---------------------------------------------------------------------------
variable "db_name" {
  description = "Nome do banco de dados inicial."
  type        = string
  default     = "appuork"
}

variable "db_username" {
  description = "Usuario master do PostgreSQL."
  type        = string
  default     = "appuork"
}

variable "db_engine_version" {
  description = "Versao do engine PostgreSQL."
  type        = string
  default     = "16.4"
}

variable "db_parameter_group_family" {
  description = "Familia do parameter group (deve casar com db_engine_version)."
  type        = string
  default     = "postgres16"
}

variable "db_instance_class" {
  description = "Classe de instancia do RDS."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "Armazenamento inicial (GB)."
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Limite de autoscaling de armazenamento (GB)."
  type        = number
  default     = 100
}

variable "db_multi_az" {
  description = "Habilita Multi-AZ (recomendado em prod)."
  type        = bool
  default     = false
}

variable "db_backup_retention_period" {
  description = "Dias de retencao de backup automatico."
  type        = number
  default     = 7
}

variable "db_deletion_protection" {
  description = "Protege o RDS contra destruicao acidental."
  type        = bool
  default     = false
}

# ---------------------------------------------------------------------------
# Aplicacao / EC2
# ---------------------------------------------------------------------------
variable "app_port" {
  description = "Porta em que o backend Spring Boot escuta."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Path do health check usado pelo ALB (Spring Boot Actuator)."
  type        = string
  default     = "/actuator/health"
}

variable "ec2_instance_type" {
  description = "Tipo de instancia EC2 do backend."
  type        = string
  default     = "t3.small"
}

variable "ec2_ami_id" {
  description = "AMI a usar. Vazio = ultima Amazon Linux 2023 x86_64 via SSM."
  type        = string
  default     = ""
}

variable "ec2_root_volume_size" {
  description = "Tamanho do volume raiz da EC2 (GB)."
  type        = number
  default     = 20
}

variable "java_package" {
  description = "Pacote do runtime Java instalado via dnf na Amazon Linux 2023."
  type        = string
  default     = "java-21-amazon-corretto-headless"
}

variable "artifact_s3_uri" {
  description = "URI S3 do .jar do backend (ex.: s3://meu-bucket/appuork/app.jar). Vazio = deploy manual/CI."
  type        = string
  default     = ""
}

# ---------------------------------------------------------------------------
# Load balancer / TLS
# ---------------------------------------------------------------------------
variable "enable_https" {
  description = "Cria listener HTTPS 443 e redireciona 80->443. Exige certificate_arn."
  type        = bool
  default     = false
}

variable "certificate_arn" {
  description = "ARN do certificado ACM para o listener HTTPS."
  type        = string
  default     = ""
}

variable "allowed_http_cidrs" {
  description = "CIDRs autorizados a acessar o ALB (HTTP/HTTPS)."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

# ---------------------------------------------------------------------------
# Secrets Manager - variaveis de ambiente da aplicacao
# ---------------------------------------------------------------------------
variable "app_environment_secrets" {
  description = <<-EOT
    Mapa de variaveis de ambiente extras da aplicacao. Sao usadas apenas para
    semear o secret "<projeto>-<env>/app" na primeira criacao. Depois disso o
    time edita o valor direto no Secrets Manager (Terraform ignora mudancas).
  EOT
  type        = map(string)
  default     = {}
  sensitive   = true
}

# ---------------------------------------------------------------------------
# S3 - bucket da aplicacao
# ---------------------------------------------------------------------------
variable "app_bucket_force_destroy" {
  description = "Permite destruir o bucket mesmo com objetos dentro (use true apenas em staging)."
  type        = bool
  default     = false
}

variable "s3_noncurrent_version_expiration_days" {
  description = "Dias ate expirar versoes antigas de objetos no bucket da aplicacao."
  type        = number
  default     = 90
}

variable "enable_s3_access_logging" {
  description = "Cria um bucket de logs e habilita server access logging no bucket da aplicacao."
  type        = bool
  default     = true
}

variable "s3_logs_expiration_days" {
  description = "Dias de retencao dos logs de acesso do S3."
  type        = number
  default     = 90
}

variable "tags" {
  description = "Tags adicionais aplicadas a todos os recursos."
  type        = map(string)
  default     = {}
}
