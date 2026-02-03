# ------------------------------------------------
# 공통 SSM 관리용 IAM Role 및 Instance Profile (NAT 등 공통 사용)
# ------------------------------------------------
resource "aws_iam_role" "muroom_common_ssm_role" {
  name = "muroom-common-ssm-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}
resource "aws_iam_role_policy_attachment" "muroom_common_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_common_ssm_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}
resource "aws_iam_instance_profile" "muroom_common_ssm_profile" {
  name = "muroom-common-ssm-profile"
  role = aws_iam_role.muroom_common_ssm_role.name
}

# ------------------------------------------------
# PostgreSQL 전용(SSM + S3 백업 + Secrets) 접근용 IAM Role 및 Instance Profile
# ------------------------------------------------
resource "aws_iam_role" "muroom_postgres_prod_instance_role" {
  name = "muroom-postgres-prod-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}
resource "aws_iam_role" "muroom_postgres_dev_instance_role" {
  name = "muroom-postgres-dev-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_postgres_prod_instance_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_postgres_prod_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}
resource "aws_iam_role_policy_attachment" "muroom_postgres_dev_instance_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_postgres_dev_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "muroom_postgres_prod_backup_s3_policy" {
  name = "muroom-postgres-prod-backup-s3-policy"
  role = aws_iam_role.muroom_postgres_prod_instance_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
        Resource = ["${aws_s3_bucket.muroom_prod_postgres_backup.arn}/*"]
      },
      {
        Effect   = "Allow",
        Action   = ["s3:ListBucket"],
        Resource = [aws_s3_bucket.muroom_prod_postgres_backup.arn]
      }
    ]
  })
}
resource "aws_iam_role_policy" "muroom_postgres_dev_backup_s3_policy" {
  name = "muroom-postgres-dev-backup-s3-policy"
  role = aws_iam_role.muroom_postgres_dev_instance_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
        Resource = ["${aws_s3_bucket.muroom_dev_postgres_backup.arn}/*"]
      },
      {
        Effect   = "Allow",
        Action   = ["s3:ListBucket"],
        Resource = [aws_s3_bucket.muroom_dev_postgres_backup.arn]
      }
    ]
  })
}

resource "aws_iam_instance_profile" "muroom_postgres_prod_instance_profile" {
  name = "muroom-postgres-prod-instance-profile"
  role = aws_iam_role.muroom_postgres_prod_instance_role.name
}
resource "aws_iam_instance_profile" "muroom_postgres_dev_instance_profile" {
  name = "muroom-postgres-dev-instance-profile"
  role = aws_iam_role.muroom_postgres_dev_instance_role.name
}

# ------------------------------------------------
# 3. Valkey 전용 (SSM + Secrets)
# ------------------------------------------------
resource "aws_iam_role" "muroom_valkey_prod_instance_role" {
  name = "muroom-valkey-prod-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}
resource "aws_iam_role" "muroom_valkey_dev_instance_role" {
  name = "muroom-valkey-dev-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_valkey_prod_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_valkey_prod_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}
resource "aws_iam_role_policy_attachment" "muroom_valkey_dev_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_valkey_dev_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "muroom_valkey_prod_backup_s3_policy" {
  name = "muroom-valkey-prod-backup-s3-policy"
  role = aws_iam_role.muroom_valkey_prod_instance_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = ["s3:PutObject", "s3:ListBucket"],
      Resource = [aws_s3_bucket.muroom_prod_valkey_backup.arn, "${aws_s3_bucket.muroom_prod_valkey_backup.arn}/*"]
    }]
  })
}
resource "aws_iam_role_policy" "muroom_valkey_dev_backup_s3_policy" {
  name = "muroom-valkey-dev-backup-s3-policy"
  role = aws_iam_role.muroom_valkey_dev_instance_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = ["s3:PutObject", "s3:ListBucket"],
      Resource = [aws_s3_bucket.muroom_dev_valkey_backup.arn, "${aws_s3_bucket.muroom_dev_valkey_backup.arn}/*"]
    }]
  })
}

resource "aws_iam_instance_profile" "muroom_valkey_prod_instance_profile" {
  name = "muroom-valkey-prod-instance-profile"
  role = aws_iam_role.muroom_valkey_prod_instance_role.name
}
resource "aws_iam_instance_profile" "muroom_valkey_dev_instance_profile" {
  name = "muroom-valkey-dev-instance-profile"
  role = aws_iam_role.muroom_valkey_dev_instance_role.name
}

# ------------------------------------------------
# ECS Cluster의 EC2 인스턴스용 (SSM 포함)
# ------------------------------------------------
resource "aws_iam_role" "muroom_ecs_prod_instance_role" {
  name = "muroom-ecs-prod-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}
resource "aws_iam_role" "muroom_ecs_dev_instance_role" {
  name = "muroom-ecs-dev-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_prod_instance_ecs_standard_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}
resource "aws_iam_role_policy_attachment" "muroom_ecs_prod_instance_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_dev_instance_ecs_standard_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}
resource "aws_iam_role_policy_attachment" "muroom_ecs_dev_instance_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "muroom_ecs_prod_instance_profile" {
  name = "muroom-ecs-prod-instance-profile"
  role = aws_iam_role.muroom_ecs_prod_instance_role.name
}
resource "aws_iam_instance_profile" "muroom_ecs_dev_instance_profile" {
  name = "muroom-ecs-dev-instance-profile"
  role = aws_iam_role.muroom_ecs_dev_instance_role.name
}

# ---------------------------------------------------------
# ECS Task 전용 (Execution Role: 이미지 풀링 및 환경변수 주입 & Task Role: 애플리케이션 코드 내에서 AWS 자원 접근)
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_ecs_prod_task_execution_role" {
  name = "muroom-ecs-prod-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
  tags = { Name = "muroom-ecs-prod-task-execution-role" }
}
resource "aws_iam_role" "muroom_ecs_dev_task_execution_role" {
  name = "muroom-ecs-dev-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
  tags = { Name = "muroom-ecs-dev-task-execution-role" }
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_prod_task_execution_standard_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
resource "aws_iam_role_policy_attachment" "muroom_ecs_dev_task_execution_standard_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "muroom_ecs_prod_task_role" {
  name = "muroom-ecs-prod-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}
resource "aws_iam_role" "muroom_ecs_dev_task_role" {
  name = "muroom-ecs-dev-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

# ---------------------------------------------------------
# S3 서비스 접근 정책 (ECS Task Role: 앱 코드에서 파일 업로드/다운로드용)
# ---------------------------------------------------------
resource "aws_iam_policy" "muroom_prod_s3_access_policy" {
  name        = "muroom-prod-s3-access-policy"
  description = "Allow ECS Prod tasks to access Prod S3 buckets"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
        Resource = [
          "${aws_s3_bucket.muroom_prod_public_storage.arn}/*",
          "${aws_s3_bucket.muroom_prod_private_storage.arn}/*"
        ]
      },
      {
        Effect = "Allow",
        Action = ["s3:ListBucket"],
        Resource = [
          aws_s3_bucket.muroom_prod_public_storage.arn,
          aws_s3_bucket.muroom_prod_private_storage.arn
        ]
      }
    ]
  })
}
resource "aws_iam_policy" "muroom_dev_s3_access_policy" {
  name        = "muroom-dev-s3-access-policy"
  description = "Allow ECS Dev tasks to access Dev S3 buckets"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
        Resource = [
          "${aws_s3_bucket.muroom_dev_public_storage.arn}/*",
          "${aws_s3_bucket.muroom_dev_private_storage.arn}/*"
        ]
      },
      {
        Effect = "Allow",
        Action = ["s3:ListBucket"],
        Resource = [
          aws_s3_bucket.muroom_dev_public_storage.arn,
          aws_s3_bucket.muroom_dev_private_storage.arn
        ]
      }
    ]
  })
}

# 정책을 각 Task Role에 부착
resource "aws_iam_role_policy_attachment" "muroom_prod_s3_task_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_task_role.name
  policy_arn = aws_iam_policy.muroom_prod_s3_access_policy.arn
}
resource "aws_iam_role_policy_attachment" "muroom_dev_s3_task_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_task_role.name
  policy_arn = aws_iam_policy.muroom_dev_s3_access_policy.arn
}

# ---------------------------------------------------------
# Secrets Manager 접근 정책
# ---------------------------------------------------------
resource "aws_iam_policy" "muroom_prod_secretsmanager_access_policy" {
  name        = "muroom_prod_secretsmanager_access_policy"
  description = "Allow ECS Prod tasks to access Prod Secrets Manager secrets"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = ["secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret"],
        Resource = [
          aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn,
          aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn,
          aws_secretsmanager_secret.muroom_jwt_secret_key_prod_secret_manager.arn,
          aws_secretsmanager_secret.muroom_api_keys_prod_secret_manager.arn,
        ]
      }
    ]
  })
}
resource "aws_iam_policy" "muroom_dev_secretsmanager_access_policy" {
  name        = "muroom_dev_secretsmanager_access_policy"
  description = "Allow ECS Dev tasks to access Dev Secrets Manager secrets"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ],
        Resource = [
          aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn,
          aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn,
          aws_secretsmanager_secret.muroom_jwt_secret_key_dev_secret_manager.arn,
          aws_secretsmanager_secret.muroom_api_keys_dev_secret_manager.arn,
        ]
      }
    ]
  })
}

# --- Secrets 권한 연결 (Postgres, Valkey, ECS Execution, ECS Task 모두) ---
# Postgres
resource "aws_iam_role_policy_attachment" "muroom_postgres_prod_instance_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_postgres_prod_instance_role.name
  policy_arn = aws_iam_policy.muroom_prod_secretsmanager_access_policy.arn
}
resource "aws_iam_role_policy_attachment" "muroom_postgres_dev_instance_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_postgres_dev_instance_role.name
  policy_arn = aws_iam_policy.muroom_dev_secretsmanager_access_policy.arn
}

# Valkey
resource "aws_iam_role_policy_attachment" "muroom_valkey_prod_instance_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_valkey_prod_instance_role.name
  policy_arn = aws_iam_policy.muroom_prod_secretsmanager_access_policy.arn
}
resource "aws_iam_role_policy_attachment" "muroom_valkey_dev_instance_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_valkey_dev_instance_role.name
  policy_arn = aws_iam_policy.muroom_dev_secretsmanager_access_policy.arn
}

# ECS Execution (환경변수 주입용)
resource "aws_iam_role_policy_attachment" "muroom_prod_task_execution_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_task_execution_role.name
  policy_arn = aws_iam_policy.muroom_prod_secretsmanager_access_policy.arn
}
resource "aws_iam_role_policy_attachment" "muroom_dev_task_execution_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_task_execution_role.name
  policy_arn = aws_iam_policy.muroom_dev_secretsmanager_access_policy.arn
}

# ECS Task (앱 코드 직접 호출용)
resource "aws_iam_role_policy_attachment" "muroom_prod_task_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_prod_task_role.name
  policy_arn = aws_iam_policy.muroom_prod_secretsmanager_access_policy.arn
}
resource "aws_iam_role_policy_attachment" "muroom_dev_task_secretsmanager_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_dev_task_role.name
  policy_arn = aws_iam_policy.muroom_dev_secretsmanager_access_policy.arn
}

# ---------------------------------------------------------
# DLM(데이터 수명 주기 관리)용 IAM Role 생성
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_dlm_role" {
  name = "muroom-dlm-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17", Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "dlm.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "muroom_dlm_policy" {
  name = "muroom-dlm-policy"
  role = aws_iam_role.muroom_dlm_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "ec2:CreateSnapshot",
          "ec2:CreateSnapshots",
          "ec2:CopySnapshot",
          "ec2:DeleteSnapshot",
          "ec2:DescribeInstances",
          "ec2:DescribeVolumes",
          "ec2:DescribeSnapshots",
        ],
        Resource = "*",
      },
      {
        Effect   = "Allow",
        Action   = ["ec2:CreateTags"],
        Resource = "arn:aws:ec2:*::snapshot/*",
      },
    ],
  })
}

# ---------------------------------------------------------
# CloudWatch Logs 접근 정책 (ECS Task Execution Role에 연결)
# ---------------------------------------------------------
resource "aws_iam_role_policy" "muroom_ecs_prod_task_execution_logging_policy" {
  name = "muroom-ecs-prod-task-execution-logging-policy"
  role = aws_iam_role.muroom_ecs_prod_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = ["${aws_cloudwatch_log_group.muroom_ecs_prod_log_group.arn}:*"]
      }
    ]
  })
}
resource "aws_iam_role_policy" "muroom_ecs_dev_task_execution_logging_policy" {
  name = "muroom-ecs-dev-task-execution-logging-policy"
  role = aws_iam_role.muroom_ecs_dev_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = ["${aws_cloudwatch_log_group.muroom_ecs_dev_log_group.arn}:*"]
      }
    ]
  })
}
