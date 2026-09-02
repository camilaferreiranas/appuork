# ---------------------------------------------------------------------------
# KMS - chave dedicada para RDS e Secrets Manager
# ---------------------------------------------------------------------------
resource "aws_kms_key" "main" {
  description             = "${local.name} - criptografia de RDS e Secrets Manager"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(local.common_tags, { Name = "${local.name}-kms" })
}

resource "aws_kms_alias" "main" {
  name          = "alias/${local.name}"
  target_key_id = aws_kms_key.main.key_id
}

# ---------------------------------------------------------------------------
# Senha do usuario master do banco (gerada e nunca versionada em texto puro)
# ---------------------------------------------------------------------------
resource "random_password" "db_master" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}"
}

# ---------------------------------------------------------------------------
# Secret 1: credenciais e string de conexao do banco (100% gerenciado por TF)
# ---------------------------------------------------------------------------
resource "aws_secretsmanager_secret" "db" {
  name                    = "${local.name}/database"
  description             = "Credenciais master e dados de conexao do PostgreSQL - ${local.name}"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id

  secret_string = jsonencode({
    engine                     = "postgres"
    host                       = aws_db_instance.this.address
    port                       = aws_db_instance.this.port
    dbname                     = var.db_name
    username                   = var.db_username
    password                   = random_password.db_master.result
    SPRING_DATASOURCE_URL      = "jdbc:postgresql://${aws_db_instance.this.address}:${aws_db_instance.this.port}/${var.db_name}"
    SPRING_DATASOURCE_USERNAME = var.db_username
    SPRING_DATASOURCE_PASSWORD = random_password.db_master.result
  })
}

# ---------------------------------------------------------------------------
# Secret 2: variaveis de ambiente da aplicacao.
# Semeado uma vez a partir de var.app_environment_secrets; depois o time
# gerencia o conteudo direto no console/CLI do Secrets Manager.
# ---------------------------------------------------------------------------
resource "aws_secretsmanager_secret" "app" {
  name                    = "${local.name}/app"
  description             = "Variaveis de ambiente da aplicacao - ${local.name}"
  kms_key_id              = aws_kms_key.main.arn
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id

  secret_string = jsonencode(merge(
    {
      SPRING_PROFILES_ACTIVE = var.environment
      SERVER_PORT            = tostring(var.app_port)
    },
    var.app_environment_secrets,
  ))

  lifecycle {
    ignore_changes = [secret_string]
  }
}
