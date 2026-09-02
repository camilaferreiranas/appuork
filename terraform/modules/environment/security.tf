# ---------------------------------------------------------------------------
# Security groups - fluxo: Internet -> ALB -> EC2 (app) -> RDS
# ---------------------------------------------------------------------------
resource "aws_security_group" "alb" {
  name_prefix = "${local.name}-alb-"
  description = "ALB - entrada HTTP/HTTPS da internet"
  vpc_id      = aws_vpc.this.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.allowed_http_cidrs
  }

  dynamic "ingress" {
    for_each = var.enable_https ? [1] : []
    content {
      description = "HTTPS"
      from_port   = 443
      to_port     = 443
      protocol    = "tcp"
      cidr_blocks = var.allowed_http_cidrs
    }
  }

  egress {
    description = "Saida liberada"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(local.common_tags, { Name = "${local.name}-alb-sg" })
}

resource "aws_security_group" "app" {
  name_prefix = "${local.name}-app-"
  description = "EC2 backend - trafego apenas a partir do ALB"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "Porta da aplicacao a partir do ALB"
    from_port       = var.app_port
    to_port         = var.app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    description = "Saida liberada (RDS, Secrets Manager, updates, SSM)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(local.common_tags, { Name = "${local.name}-app-sg" })
}

resource "aws_security_group" "db" {
  name_prefix = "${local.name}-db-"
  description = "RDS PostgreSQL - trafego apenas a partir das instancias da aplicacao"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "PostgreSQL a partir do backend"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(local.common_tags, { Name = "${local.name}-db-sg" })
}
