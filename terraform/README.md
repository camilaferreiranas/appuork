# Infraestrutura AWS - appuork (Terraform)

Provisiona dois ambientes isolados (**staging** e **prod**) para o backend Spring Boot
`appuork` com banco **PostgreSQL gerenciado (RDS)**, seguindo boas práticas de segurança.

## Arquitetura

```
                 Internet
                    │  (80/443)
            ┌───────▼────────┐
            │      ALB       │  subnets públicas (2 AZs)
            └───────┬────────┘
                    │  8080 (só do SG do ALB)
        ┌───────────▼───────────┐
        │   EC2 backend (jar)   │  subnet privada  ─── SSM Session Manager (sem SSH)
        │   systemd: appuork    │
        └───────────┬───────────┘
                    │  5432 (só do SG da app)
        ┌───────────▼───────────┐
        │  RDS PostgreSQL       │  subnets privadas, storage criptografado (KMS)
        │  (Multi-AZ em prod)   │  rds.force_ssl = 1
        └───────────────────────┘

Secrets Manager (KMS):
  <projeto>-<env>/database  → credenciais + string de conexão (gerenciado 100% por TF)
  <projeto>-<env>/app       → variáveis de ambiente da aplicação (editável pelo time)

S3 (KMS, privado, TLS-only):
  <projeto>-<env>-app-<acct>   → uploads/anexos da aplicação (a EC2 lê/grava)
  <projeto>-<env>-logs-<acct>  → server access logs do bucket acima
```

### Decisões de segurança

| Item | Implementação |
|------|---------------|
| Banco nunca exposto | RDS `publicly_accessible = false`, em subnet privada |
| Segmentação de rede | 3 security groups encadeados: ALB → App → DB |
| Sem SSH / bastion | Acesso ao shell via **SSM Session Manager** (`AmazonSSMManagedInstanceCore`) |
| Criptografia em repouso | RDS, EBS e Secrets Manager com KMS (chave dedicada por ambiente, rotação anual) |
| Criptografia em trânsito | `rds.force_ssl = 1`; listener HTTPS/redirect 80→443 quando `enable_https = true` |
| Senha do banco | `random_password` de 32 chars, só existe no state e no Secrets Manager |
| Metadados da EC2 | IMDSv2 obrigatório (`http_tokens = required`) |
| Menor privilégio | Role da EC2 só lê os 2 secrets do próprio ambiente + `kms:Decrypt` da própria chave |
| Proteção contra perda | `deletion_protection` + `final_snapshot` + backups 30d em prod |
| Bucket S3 da app | Privado (PAB total), ACLs desabilitadas (`BucketOwnerEnforced`), SSE-KMS obrigatória, versionamento, lifecycle, policy `Deny` sem TLS e `Deny` upload sem a KMS correta, access logging |
| State remoto | S3 versionado/criptografado + lock no DynamoDB |

## Estrutura

```
terraform/
├── bootstrap/              # cria bucket S3 + tabela DynamoDB para o state remoto
├── modules/
│   └── environment/        # stack completo de um ambiente (rede, SG, RDS, EC2, ALB, S3, secrets, IAM)
└── environments/
    ├── staging/            # instância deste módulo p/ staging
    └── prod/               # instância deste módulo p/ prod (HA)
```

## Pré-requisitos

- Terraform >= 1.5
- AWS CLI configurado (`aws sts get-caller-identity` deve funcionar)
- Permissões para criar VPC, EC2, RDS, ELB, IAM, KMS, Secrets Manager, S3

## Passo a passo

### 1. Criar o backend de state (uma vez por conta)

```bash
cd bootstrap
terraform init
terraform apply -var="state_bucket_name=appuork-tfstate-<SEU_ACCOUNT_ID>"
```

### 2. Provisionar staging

```bash
cd ../environments/staging
cp backend.hcl.example backend.hcl            # ajuste o nome do bucket
cp terraform.tfvars.example terraform.tfvars  # ajuste região, TLS, secrets...

terraform init -backend-config=backend.hcl
terraform plan
terraform apply
```

### 3. Provisionar prod

```bash
cd ../prod
cp backend.hcl.example backend.hcl
cp terraform.tfvars.example terraform.tfvars   # defina certificate_arn (TLS obrigatório)

terraform init -backend-config=backend.hcl
terraform apply
```

## Deploy da aplicação

O `user_data` da EC2:
1. instala o runtime Java (`java_package`, padrão Corretto 21 — ajuste para casar com o build);
2. lê os 2 secrets do ambiente e gera `/opt/appuork/appuork.env`;
3. registra o serviço systemd `appuork` (roda `java -jar /opt/appuork/app.jar`);
4. se `artifact_s3_uri` estiver definido, baixa o jar e sobe o serviço.

**Opção A — via S3 (recomendado):** publique o jar no S3 e defina `artifact_s3_uri`.
Para atualizar depois:

```bash
INSTANCE_ID=$(terraform output -raw app_instance_id)
aws ssm start-session --target "$INSTANCE_ID"
# na instância:
sudo aws s3 cp s3://.../app.jar /opt/appuork/app.jar
sudo systemctl restart appuork
```

**Opção B — recriar a instância:** rode `terraform apply` após publicar novo jar
(o `user_data` roda de novo se mudar).

## Variáveis de ambiente / secrets

- **`<projeto>-<env>/database`**: gerado e mantido pelo Terraform. Contém `host`, `port`,
  `username`, `password`, `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`.
- **`<projeto>-<env>/app`**: semeado uma vez a partir de `app_environment_secrets` no
  `terraform.tfvars`. Depois disso, **edite direto no Secrets Manager** — o Terraform
  ignora mudanças no conteúdo (`ignore_changes`). Reinicie a app para recarregar.

```bash
aws secretsmanager put-secret-value \
  --secret-id appuork-prod/app \
  --secret-string '{"SPRING_PROFILES_ACTIVE":"prod","SERVER_PORT":"8080","JWT_SECRET":"..."}'
```

## Bucket S3 da aplicação

Cada ambiente tem um bucket `appuork-<env>-app-<account_id>`:

- **Privado**: public access block total, ACLs desabilitadas (`BucketOwnerEnforced`).
- **Criptografia**: SSE-KMS com a chave do ambiente + Bucket Keys. A bucket policy
  **rejeita** `PutObject` que não use exatamente essa chave e **rejeita** qualquer
  requisição sem TLS.
- **Versionamento** ligado; lifecycle expira versões antigas (`s3_noncurrent_version_expiration_days`)
  e aborta uploads multipart incompletos após 7 dias.
- **Access logging** para `appuork-<env>-logs-<account_id>` (desligável via
  `enable_s3_access_logging = false`).
- A role da EC2 recebe `s3:GetObject/PutObject/DeleteObject/ListBucket` só nesse bucket
  e `kms:GenerateDataKey/Encrypt/Decrypt` só na chave do ambiente.

O nome do bucket chega à aplicação como env var `APP_S3_BUCKET` (via `user_data`), e
também está no output `app_bucket_name`.

> Em **staging**, `app_bucket_force_destroy = true` permite `terraform destroy` com
> objetos dentro. Em **prod** é `false` — esvazie o bucket antes de destruir.

## Custo aproximado (us-east-1, on-demand)

| Recurso | staging | prod |
|---|---|---|
| EC2 | t3.small (~US$15/mês) | t3.medium (~US$30/mês) |
| RDS | db.t4g.micro single-AZ (~US$13/mês) | db.t4g.medium Multi-AZ (~US$100/mês) |
| ALB | ~US$18/mês | ~US$18/mês |
| NAT Gateway | ~US$33/mês + tráfego | ~US$33/mês + tráfego |

Para reduzir custo em staging, é possível trocar o NAT Gateway por endpoints de
interface (SSM, Secrets Manager, ECR) ou usar uma instância NAT.

## Destruir

```bash
cd environments/staging && terraform destroy
```

Em **prod**, remova antes `db_deletion_protection` e o `enable_deletion_protection`
do ALB (ou faça `terraform apply` com esses valores em `false`).
