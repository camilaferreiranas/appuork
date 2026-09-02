variable "aws_region" {
  description = "Regiao AWS."
  type        = string
  default     = "us-east-1"
}

variable "artifact_s3_uri" {
  description = "URI S3 do .jar do backend (ex.: s3://bucket/appuork/app.jar)."
  type        = string
  default     = ""
}

variable "enable_https" {
  description = "Habilita listener HTTPS 443 no ALB (exige certificate_arn)."
  type        = bool
  default     = true
}

variable "certificate_arn" {
  description = "ARN do certificado ACM."
  type        = string
  default     = ""
}

variable "allowed_http_cidrs" {
  description = "CIDRs autorizados a acessar o ALB."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "app_environment_secrets" {
  description = "Variaveis de ambiente extras semeadas no Secrets Manager."
  type        = map(string)
  default     = {}
  sensitive   = true
}
