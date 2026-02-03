# --------------------------------------------------------
# Secrets Manager에 prod DB 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "muroom_postgres_prod_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}:<>?"
}
resource "aws_secretsmanager_secret" "muroom_postgres_prod_secret_manager" {
  name                    = "muroom/postgres-prod/credentials"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-postgres-prod-credentials" }
}
resource "aws_secretsmanager_secret_version" "muroom_postgres_prod_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.id
  secret_string = jsonencode({
    engine   = "postgres"
    dbname   = var.postgres_prod_dbname
    host     = aws_instance.muroom_postgres_prod_instance.private_ip
    port     = var.postgres_prod_port
    username = var.postgres_prod_username
    password = random_password.muroom_postgres_prod_password.result
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}
resource "aws_secretsmanager_secret_rotation" "muroom_postgres_prod_credential_rotation" {
  secret_id           = aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.id
  rotation_lambda_arn = aws_serverlessapplicationrepository_cloudformation_stack.muroom_postgres_credential_rotation_lambda.outputs.RotationLambdaARN

  rotation_rules {
    automatically_after_days = 7
  }

  depends_on = [
    aws_serverlessapplicationrepository_cloudformation_stack.muroom_postgres_credential_rotation_lambda
  ]
}

# --------------------------------------------------------
# Secrets Manager에 dev DB 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "muroom_postgres_password_dev" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}:<>?"
}
resource "aws_secretsmanager_secret" "muroom_postgres_dev_secret_manager" {
  name                    = "muroom/postgres-dev/credentials"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-postgres-dev-credentials" }
}
resource "aws_secretsmanager_secret_version" "muroom_postgres_dev_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.id
  secret_string = jsonencode({
    engine   = "postgres"
    dbname   = var.postgres_dev_dbname
    host     = aws_instance.muroom_postgres_dev_instance.private_ip
    port     = var.postgres_dev_port
    username = var.postgres_dev_username
    password = random_password.muroom_postgres_password_dev.result
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}
resource "aws_secretsmanager_secret_rotation" "muroom_postgres_dev_credential_rotation" {
  secret_id           = aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.id
  rotation_lambda_arn = aws_serverlessapplicationrepository_cloudformation_stack.muroom_postgres_credential_rotation_lambda.outputs.RotationLambdaARN

  rotation_rules {
    automatically_after_days = 1
  }

  depends_on = [
    aws_serverlessapplicationrepository_cloudformation_stack.muroom_postgres_credential_rotation_lambda
  ]
}

# --------------------------------------------------------
# Secrets Manager에 prod Valkey 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "muroom_valkey_prod_password" {
  length  = 16
  special = false
}
resource "aws_secretsmanager_secret" "muroom_valkey_prod_secret_manager" {
  name                    = "muroom/valkey-prod/credentials"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-valkey-prod-credentials" }
}
resource "aws_secretsmanager_secret_version" "muroom_valkey_prod_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.id
  secret_string = jsonencode({
    host     = aws_instance.muroom_valkey_prod_instance.private_ip
    port     = var.valkey_prod_port
    username = var.valkey_prod_username
    password = random_password.muroom_valkey_prod_password.result
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}

# --------------------------------------------------------
# Secrets Manager에 dev Valkey 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "muroom_valkey_dev_password" {
  length  = 16
  special = false
}
resource "aws_secretsmanager_secret" "muroom_valkey_dev_secret_manager" {
  name                    = "muroom/valkey-dev/credentials"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-valkey-dev-credentials" }
}
resource "aws_secretsmanager_secret_version" "muroom_valkey_dev_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.id
  secret_string = jsonencode({
    host     = aws_instance.muroom_valkey_dev_instance.private_ip
    port     = var.valkey_dev_port
    username = var.valkey_dev_username
    password = random_password.muroom_valkey_dev_password.result
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}

# --------------------------------------------------------
# JWT Secret Keys (TODO: Deprecate and Delete)
# --------------------------------------------------------
resource "random_password" "muroom_jwt_secret_key_prod" {
  length  = 64
  special = true
}
resource "aws_secretsmanager_secret" "muroom_jwt_secret_key_prod_secret_manager" {
  name                    = "muroom/jwt-secret-key-prod"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-jwt-secret-key-prod" }
}
resource "aws_secretsmanager_secret_version" "muroom_jwt_secret_key_prod_secret_version" {
  secret_id     = aws_secretsmanager_secret.muroom_jwt_secret_key_prod_secret_manager.id
  secret_string = random_password.muroom_jwt_secret_key_prod.result
}
resource "random_password" "muroom_jwt_secret_key_dev" {
  length  = 64
  special = true
}
resource "aws_secretsmanager_secret" "muroom_jwt_secret_key_dev_secret_manager" {
  name                    = "muroom/jwt-secret-key-dev"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-jwt-secret-key-dev" }
}
resource "aws_secretsmanager_secret_version" "muroom_jwt_secret_key_dev_secret_version" {
  secret_id     = aws_secretsmanager_secret.muroom_jwt_secret_key_dev_secret_manager.id
  secret_string = random_password.muroom_jwt_secret_key_dev.result
}


# --------------------------------------------------------
# 그 외 애플리케이션 비밀키들 (비용 절감 차원에서 하나로 관리)
# --------------------------------------------------------
resource "aws_secretsmanager_secret" "muroom_api_keys_prod_secret_manager" {
  name                    = "muroom/api-keys-prod"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-api-keys-prod" }
}
resource "aws_secretsmanager_secret_version" "muroom_api_keys_prod_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.id
  secret_string = jsonencode({
    seoul_api_key         = var.seoul_api_key_prod
    juso_search_api_key   = var.juso_search_api_key_prod
    juso_coord_api_key    = var.juso_coord_api_key_prod
    kakao_rest_api_key    = var.kakao_rest_api_key_prod
    kakao_client_id       = var.kakao_client_id_prod
    kakao_client_secret   = var.kakao_client_secret_prod
    google_client_id      = var.google_client_id_prod
    google_client_secret  = var.google_client_secret_prod
    naver_access_key_id   = var.naver_access_key_id_prod
    naver_secret_key      = var.naver_secret_key_prod
    naver_sens_service_id = var.naver_sens_service_id_prod
    naver_sens_from       = var.naver_sens_from_prod
  })
}
resource "aws_secretsmanager_secret" "muroom_api_keys_dev_secret_manager" {
  name                    = "muroom/api-keys-dev"
  recovery_window_in_days = 7
  tags                    = { Name = "muroom-api-keys-dev" }
}
resource "aws_secretsmanager_secret_version" "muroom_api_keys_dev_secret_version" {
  secret_id = aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.id
  secret_string = jsonencode({
    seoul_api_key         = var.seoul_api_key_dev
    juso_search_api_key   = var.juso_search_api_key_dev
    juso_coord_api_key    = var.juso_coord_api_key_dev
    kakao_rest_api_key    = var.kakao_rest_api_key_dev
    kakao_client_id       = var.kakao_client_id_dev
    kakao_client_secret   = var.kakao_client_secret_dev
    google_client_id      = var.google_client_id_dev
    google_client_secret  = var.google_client_secret_dev
    naver_access_key_id   = var.naver_access_key_id_dev
    naver_secret_key      = var.naver_secret_key_dev
    naver_sens_service_id = var.naver_sens_service_id_dev
    naver_sens_from       = var.naver_sens_from_dev
  })
}
