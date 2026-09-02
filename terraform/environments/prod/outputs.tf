output "alb_dns_name" {
  description = "DNS publico do ALB - aponte seu dominio para este endereco."
  value       = module.stack.alb_dns_name
}

output "app_instance_id" {
  description = "ID da EC2 do backend (acesso via: aws ssm start-session --target <id>)."
  value       = module.stack.app_instance_id
}

output "rds_endpoint" {
  description = "Endpoint do RDS PostgreSQL."
  value       = module.stack.rds_endpoint
}

output "db_secret_arn" {
  description = "ARN do secret de credenciais do banco."
  value       = module.stack.db_secret_arn
}

output "app_secret_arn" {
  description = "ARN do secret de variaveis de ambiente da aplicacao."
  value       = module.stack.app_secret_arn
}

output "app_bucket_name" {
  description = "Bucket S3 da aplicacao."
  value       = module.stack.app_bucket_name
}

output "vpc_id" {
  value = module.stack.vpc_id
}
