# --------------------------------------------------------
# 1. 비용 절감형 로그 그룹 설정 (보관 기간 1일)
# --------------------------------------------------------
resource "aws_cloudwatch_log_group" "muroom_ecs_prod_log_group" {
  name              = "/ecs/muroom-prod"
  retention_in_days = 14
}

resource "aws_cloudwatch_log_group" "muroom_ecs_dev_log_group" {
  name              = "/ecs/muroom-dev"
  retention_in_days = 3
}

# --------------------------------------------------------
# 1. ECS 태스크 정의 (운영용 및 개발용)
# --------------------------------------------------------
resource "aws_ecs_task_definition" "muroom_ecs_task_definition_prod" {
  family                   = "muroom-ecs-task-def-prod"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  cpu                      = "2048" # t4g.medium (2vCPU, 4GB)에 맞춘 설정
  memory                   = "3072" # 3GB (인스턴스에 여유 메모리 확보)

  task_role_arn      = aws_iam_role.muroom_ecs_prod_task_role.arn
  execution_role_arn = aws_iam_role.muroom_ecs_prod_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name         = "muroom-backend-bach-container-prod"
      image        = "${aws_ecr_repository.muroom_backend_bach.repository_url}:prod-latest" #prod-v26.02.04/A"
      cpu          = 2048
      memory       = 3072
      essential    = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }],

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.muroom_ecs_prod_log_group.name
          "awslogs-region"        = data.aws_region.current.id
          "awslogs-stream-prefix" = "ecs"
        }
      }

      secrets = [
        # --- Database ---
        { name = "DB_NAME", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn}:dbname::" },
        { name = "DB_HOST", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn}:host::" },
        { name = "DB_PORT", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn}:port::" },
        { name = "DB_USERNAME", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn}:username::" },
        { name = "DB_PASSWORD", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn}:password::" },

        # --- Valkey ---
        { name = "VALKEY_HOST", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn}:host::" },
        { name = "VALKEY_PORT", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn}:port::" },
        { name = "VALKEY_USERNAME", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn}:username::" },
        { name = "VALKEY_PASSWORD", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn}:password::" },

        # --- JWT ---
        { name = "JWT_SECRET_KEY", valueFrom = aws_secretsmanager_secret.muroom_jwt_secret_key_prod_secret_manager.arn },

        # --- External API Keys ---
        { name = "SEOUL_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:seoul_api_key::" },
        { name = "JUSO_SEARCH_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:juso_search_api_key::" },
        { name = "JUSO_COORD_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:juso_coord_api_key::" },
        { name = "KAKAO_REST_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:kakao_rest_api_key::" },
        { name = "KAKAO_CLIENT_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:kakao_client_id::" },
        { name = "KAKAO_CLIENT_SECRET", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:kakao_client_secret::" },
        { name = "GOOGLE_CLIENT_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:google_client_id::" },
        { name = "GOOGLE_CLIENT_SECRET", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:google_client_secret::" },
        { name = "NAVER_ACCESS_KEY_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:naver_access_key_id::" },
        { name = "NAVER_SECRET_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:naver_secret_key::" },
        { name = "NAVER_SENS_SERVICE_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:naver_sens_service_id::" },
        { name = "NAVER_SENS_FROM", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn}:naver_sens_from::" }
      ],
      environment = [
        { "name" = "SPRING_PROFILES_ACTIVE", "value" = "prod" },
        { "name" = "AWS_REGION", "value" = data.aws_region.current.id },
        { "name" = "AWS_S3_PUBLIC_BUCKET", "value" = aws_s3_bucket.muroom_prod_public_storage.bucket },
        { "name" = "AWS_S3_PRIVATE_BUCKET", "value" = aws_s3_bucket.muroom_prod_private_storage.bucket },
      ],
    }
  ])
  tags = { Name = "muroom-ecs-task-def-prod" }
}

resource "aws_ecs_task_definition" "muroom_ecs_task_definition_dev" {
  family                   = "muroom-ecs-task-def-dev"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  cpu                      = "1024" # t4g.small (1vCPU, 2GB)에 맞춘 설정
  memory                   = "1536" # 1.5GB (인스턴스에 여유 메모리 확보)

  task_role_arn      = aws_iam_role.muroom_ecs_dev_task_role.arn
  execution_role_arn = aws_iam_role.muroom_ecs_dev_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name         = "muroom-backend-bach-container-dev"
      image        = "${aws_ecr_repository.muroom_backend_bach.repository_url}:dev-0c9d536"
      cpu          = 1024
      memory       = 1536
      essential    = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }],

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.muroom_ecs_dev_log_group.name
          "awslogs-region"        = data.aws_region.current.id
          "awslogs-stream-prefix" = "ecs"
        }
      }

      secrets = [
        # --- Database ---
        { name = "DB_NAME", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn}:dbname::" },
        { name = "DB_HOST", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn}:host::" },
        { name = "DB_PORT", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn}:port::" },
        { name = "DB_USERNAME", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn}:username::" },
        { name = "DB_PASSWORD", valueFrom = "${aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn}:password::" },

        # --- Valkey ---
        { name = "VALKEY_HOST", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn}:host::" },
        { name = "VALKEY_PORT", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn}:port::" },
        { name = "VALKEY_USERNAME", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn}:username::" },
        { name = "VALKEY_PASSWORD", valueFrom = "${aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn}:password::" },

        # --- JWT ---
        { name = "JWT_SECRET_KEY", valueFrom = aws_secretsmanager_secret.muroom_jwt_secret_key_dev_secret_manager.arn },

        # --- External API Keys ---
        { name = "SEOUL_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:seoul_api_key::" },
        { name = "JUSO_SEARCH_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:juso_search_api_key::" },
        { name = "JUSO_COORD_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:juso_coord_api_key::" },
        { name = "KAKAO_REST_API_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:kakao_rest_api_key::" },
        { name = "KAKAO_CLIENT_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:kakao_client_id::" },
        { name = "KAKAO_CLIENT_SECRET", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:kakao_client_secret::" },
        { name = "GOOGLE_CLIENT_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:google_client_id::" },
        { name = "GOOGLE_CLIENT_SECRET", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:google_client_secret::" },
        { name = "NAVER_ACCESS_KEY_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:naver_access_key_id::" },
        { name = "NAVER_SECRET_KEY", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:naver_secret_key::" },
        { name = "NAVER_SENS_SERVICE_ID", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:naver_sens_service_id::" },
        { name = "NAVER_SENS_FROM", valueFrom = "${aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn}:naver_sens_from::" }
      ],
      environment = [
        { "name" = "SPRING_PROFILES_ACTIVE", "value" = "dev" },
        { "name" = "AWS_REGION", "value" = data.aws_region.current.id },
        { "name" = "AWS_S3_PUBLIC_BUCKET", "value" = aws_s3_bucket.muroom_dev_public_storage.bucket },
        { "name" = "AWS_S3_PRIVATE_BUCKET", "value" = aws_s3_bucket.muroom_dev_private_storage.bucket },
      ],
    }
  ])
  tags = { Name = "muroom-ecs-task-def-dev" }
}
