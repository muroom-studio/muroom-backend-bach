resource "aws_serverlessapplicationrepository_cloudformation_stack" "muroom_db_credential_rotation_lambda" {
  name             = "muroom-db-credential-rotation-lambda-stack"
  application_id   = "arn:aws:serverlessrepo:us-east-1:297356227824:applications/SecretsManagerRDSPostgreSQLRotationSingleUser"
  semantic_version = "1.1.629"

  parameters = {
    vpcSubnetIds        = "${aws_subnet.muroom_private_subnet_2a.id},${aws_subnet.muroom_private_subnet_2b.id}"
    vpcSecurityGroupIds = aws_security_group.muroom_db_credential_rotation_lambda_sg.id
    endpoint            = "https://secretsmanager.${data.aws_region.current.name}.amazonaws.com"
    functionName        = "muroom-db-credential-rotation-lambda"
  }

  capabilities = ["CAPABILITY_IAM", "CAPABILITY_RESOURCE_POLICY", "CAPABILITY_AUTO_EXPAND"]
}

# --------------------------------------------------------
# Secrets Manager에 prod DB 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "db_prod_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}:<>?"
}
resource "aws_secretsmanager_secret" "db_prod_secret_manager" {
  name = "muroom/db-prod/credentials"
  tags = { Name = "muroom-db-prod-credentials" }
}
resource "aws_secretsmanager_secret_version" "db_prod_secret_version" {
  secret_id = aws_secretsmanager_secret.db_prod_secret_manager.id
  secret_string = jsonencode({
    dbname   = var.db_prod_dbname
    username = var.db_prod_username
    password = random_password.db_prod_password.result
  })
}
resource "aws_secretsmanager_secret_rotation" "db_prod_credential_rotation" {
  secret_id           = aws_secretsmanager_secret.db_prod_secret_manager.id
  rotation_lambda_arn = aws_serverlessapplicationrepository_cloudformation_stack.muroom_db_credential_rotation_lambda.outputs.RotationLambdaARN

  rotation_rules {
    automatically_after_days = 7
  }

  depends_on = [
    aws_serverlessapplicationrepository_cloudformation_stack.muroom_db_credential_rotation_lambda
  ]
}

# --------------------------------------------------------
# Secrets Manager에 dev DB 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "db_password_dev" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}:<>?"
}
resource "aws_secretsmanager_secret" "db_dev_secret_manager" {
  name = "muroom/db-dev/credentials"
  tags = { Name = "muroom-db-dev-credentials" }
}
resource "aws_secretsmanager_secret_version" "db_dev_secret_version" {
  secret_id = aws_secretsmanager_secret.db_dev_secret_manager.id
  secret_string = jsonencode({
    dbname   = var.db_dev_dbname
    username = var.db_dev_username
    password = random_password.db_password_dev.result
  })
}
resource "aws_secretsmanager_secret_rotation" "db_dev_credential_rotation" {
  secret_id           = aws_secretsmanager_secret.db_dev_secret_manager.id
  rotation_lambda_arn = aws_serverlessapplicationrepository_cloudformation_stack.muroom_db_credential_rotation_lambda.outputs.RotationLambdaARN

  rotation_rules {
    automatically_after_days = 1
  }

  depends_on = [
    aws_serverlessapplicationrepository_cloudformation_stack.muroom_db_credential_rotation_lambda
  ]
}

# --------------------------------------------------------
# Secrets Manager에 prod Valkey 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "valkey_prod_password" {
  length  = 16
  special = false
}
resource "aws_secretsmanager_secret" "valkey_prod_secret_manager" {
  name = "muroom/valkey-prod/credentials"
  tags = { Name = "muroom-valkey-prod-credentials" }
}
resource "aws_secretsmanager_secret_version" "valkey_prod_secret_version" {
  secret_id = aws_secretsmanager_secret.valkey_prod_secret_manager.id
  secret_string = jsonencode({
    username = var.valkey_prod_username
    password = random_password.valkey_prod_password.result
  })
}

# --------------------------------------------------------
# Secrets Manager에 dev Valkey 비밀번호를 저장하는 리소스 정의
# --------------------------------------------------------
resource "random_password" "valkey_dev_password" {
  length  = 16
  special = false
}
resource "aws_secretsmanager_secret" "valkey_dev_secret_manager" {
  name = "muroom/valkey-dev/credentials"
  tags = { Name = "muroom-valkey-dev-credentials" }
}
resource "aws_secretsmanager_secret_version" "valkey_dev_secret_version" {
  secret_id     = aws_secretsmanager_secret.valkey_dev_secret_manager.id
  secret_string = random_password.valkey_dev_password.result
}
