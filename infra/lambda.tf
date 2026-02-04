# --------------------------------------------------------
# PostgreSQL RDS 자격 증명 교체를 위한 Lambda 함수 생성
# --------------------------------------------------------
resource "aws_serverlessapplicationrepository_cloudformation_stack" "muroom_postgres_credential_rotation_lambda" {
  name             = "muroom-db-credential-rotation-lambda-stack"
  application_id   = "arn:aws:serverlessrepo:us-east-1:297356227824:applications/SecretsManagerRDSPostgreSQLRotationSingleUser"
  semantic_version = "1.1.629"

  parameters = {
    vpcSubnetIds        = "${aws_subnet.muroom_private_subnet_2a.id},${aws_subnet.muroom_private_subnet_2b.id}"
    vpcSecurityGroupIds = aws_security_group.muroom_postgres_credential_rotation_lambda_sg.id
    endpoint            = "https://secretsmanager.${data.aws_region.current.id}.amazonaws.com"
    functionName        = "muroom-db-credential-rotation-lambda"
  }

  lifecycle {
    prevent_destroy = true
  }

  capabilities = ["CAPABILITY_IAM", "CAPABILITY_RESOURCE_POLICY", "CAPABILITY_AUTO_EXPAND"]
}

resource "aws_cloudwatch_log_group" "lambda_rotation_log_group" {
  name              = "/aws/lambda/muroom-db-credential-rotation-lambda"
  retention_in_days = 7
}
