# --------------------------------------------------------
# ECS prod 환경 태스크 정의 및 서비스 설정
# --------------------------------------------------------
resource "aws_ecs_task_definition" "muroom_ecs_task_definition_prod" {
  family                   = "muroom-ecs-task-def-prod"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  cpu                      = "2048" # t4g.medium (2vCPU, 4GB)에 맞춘 설정
  memory                   = "3072" # 3GB (인스턴스에 여유 메모리 확보)

  task_role_arn      = aws_iam_role.muroom_ecs_task_role.arn
  execution_role_arn = aws_iam_role.muroom_ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name         = "muroom-backend-bach-container-prod"
      image        = "${aws_ecr_repository.muroom_backend_bach.repository_url}:prod-latest"
      cpu          = 2048
      memory       = 3072
      essential    = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }],
      secrets = [
        { name = "DB_NAME", valueFrom = "${aws_secretsmanager_secret.db_prod_secret_manager.arn}:dbname::" },
        { name = "DB_USERNAME", valueFrom = "${aws_secretsmanager_secret.db_prod_secret_manager.arn}:username::" },
        { name = "DB_PASSWORD", valueFrom = "${aws_secretsmanager_secret.db_prod_secret_manager.arn}:password::" },
        { name = "VALKEY_USERNAME", valueFrom = "${aws_secretsmanager_secret.valkey_prod_secret_manager.arn}:username::" },
        { name = "VALKEY_PASSWORD", valueFrom = "${aws_secretsmanager_secret.valkey_prod_secret_manager.arn}:password::" },
        { name = "VALKEY_API_KEY", valueFrom = "${aws_secretsmanager_secret.valkey_prod_secret_manager.arn}:api_key::" }
      ],
      environment = [
        # { "name" = "SPRING_PROFILES_ACTIVE", "value" = "prod" },
      ],
    }
  ])
  tags = { Name = "muroom-ecs-task-def-prod" }
}

# --------------------------------------------------------
# ECS dev 환경 태스크 정의 및 서비스 설정
# --------------------------------------------------------
resource "aws_ecs_task_definition" "muroom_ecs_task_definition_dev" {
  family                   = "muroom-ecs-task-def-dev"
  network_mode             = "awsvpc"
  requires_compatibilities = ["EC2"]
  cpu                      = "1024" # t4g.small (1vCPU, 2GB)에 맞춘 설정
  memory                   = "1536" # 1.5GB (인스턴스에 여유 메모리 확보)

  task_role_arn      = aws_iam_role.muroom_ecs_task_role.arn
  execution_role_arn = aws_iam_role.muroom_ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name         = "muroom-backend-bach-container-dev"
      image        = "${aws_ecr_repository.muroom_backend_bach.repository_url}:dev-latest"
      cpu          = 1024
      memory       = 1536
      essential    = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }],
      secrets      = [],
      environment  = []
    }
  ])
  tags = { Name = "muroom-ecs-task-def-dev" }
}
