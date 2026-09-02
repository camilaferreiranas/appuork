output "vpc_id" {
  description = "ID da VPC criada."
  value       = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "IDs das subnets publicas."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "IDs das subnets privadas."
  value       = aws_subnet.private[*].id
}

output "alb_dns_name" {
  description = "DNS publico do Application Load Balancer."
  value       = aws_lb.this.dns_name
}

output "alb_zone_id" {
  description = "Zone ID do ALB (para alias de Route53)."
  value       = aws_lb.this.zone_id
}

output "app_instance_id" {
  description = "ID da instancia EC2 do backend (use com SSM Session Manager)."
  value       = aws_instance.app.id
}

output "rds_endpoint" {
  description = "Endpoint (host:porta) do RDS PostgreSQL."
  value       = aws_db_instance.this.endpoint
}

output "rds_address" {
  description = "Hostname do RDS PostgreSQL."
  value       = aws_db_instance.this.address
}

output "db_secret_arn" {
  description = "ARN do secret com credenciais/conexao do banco."
  value       = aws_secretsmanager_secret.db.arn
}

output "app_secret_arn" {
  description = "ARN do secret com variaveis de ambiente da aplicacao."
  value       = aws_secretsmanager_secret.app.arn
}

output "kms_key_arn" {
  description = "ARN da chave KMS do ambiente."
  value       = aws_kms_key.main.arn
}

output "app_bucket_name" {
  description = "Nome do bucket S3 da aplicacao."
  value       = aws_s3_bucket.app.bucket
}

output "app_bucket_arn" {
  description = "ARN do bucket S3 da aplicacao."
  value       = aws_s3_bucket.app.arn
}

output "logs_bucket_name" {
  description = "Nome do bucket S3 de logs de acesso (se habilitado)."
  value       = var.enable_s3_access_logging ? aws_s3_bucket.logs[0].bucket : null
}
