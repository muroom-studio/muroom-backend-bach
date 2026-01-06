# ------------------------------------------------
# EC2 인스턴스가 SSM을 통해 관리될 수 있도록 하는 IAM Role 및 Instance Profile 생성
# ------------------------------------------------
resource "aws_iam_role" "muroom_ssm_role" {
  name = "muroom-ssm-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_ssm_managed_policy_attachment" {
  role       = aws_iam_role.muroom_ssm_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "muroom_ssm_instance_profile" {
  name = "muroom-ssm-instance-profile"
  role = aws_iam_role.muroom_ssm_role.name
}

# ---------------------------------------------------------
# ECS 클러스터의 EC2 인스턴스용 IAM Role 및 Instance Profile 생성
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_ecs_instance_role" {
  name = "muroom-ecs-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_instance_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_instance_ssm_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "muroom_ecs_instance_profile" {
  name = "muroom-ecs-instance-profile"
  role = aws_iam_role.muroom_ecs_instance_role.name
}


# ---------------------------------------------------------
# ECS 작업용 IAM Role 생성 (작업 실행 역할)
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_ecs_task_execution_role" {
  name = "muroom-ecs-task-execution-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "muroom_ecs_task_execution_policy_attachment" {
  role       = aws_iam_role.muroom_ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ---------------------------------------------------------
# ECS 작업용 IAM Role 생성 (작업 역할)
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_ecs_task_role" {
  name = "muroom-ecs-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# ---------------------------------------------------------
# DLM(데이터 수명 주기 관리)용 IAM Role 생성
# ---------------------------------------------------------
resource "aws_iam_role" "muroom_dlm_role" {
  name = "muroom-dlm-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "dlm.amazonaws.com"
        },
      },
    ],
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
# Secrets Manager에서 비밀을 읽을 수 있는 권한 정책 생성 및 역할에 연결
# ---------------------------------------------------------
resource "aws_iam_policy" "secretsmanager_read_policy" {
  name        = "muroom-secretsmanager-read-policy"
  description = "EC2 인스턴스가 Secrets Manager에서 비밀을 읽을 수 있는 권한 정책"

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
          aws_secretsmanager_secret.db_prod_secret_manager.arn,
          aws_secretsmanager_secret.db_dev_secret_manager.arn,
          aws_secretsmanager_secret.valkey_prod_secret_manager.arn,
          aws_secretsmanager_secret.valkey_dev_secret_manager.arn
        ]
      }
    ]
  })
}
resource "aws_iam_role_policy_attachment" "muroom_ssm_secretsmanager_read_policy_attachment" {
  role       = aws_iam_role.muroom_ssm_role.name
  policy_arn = aws_iam_policy.secretsmanager_read_policy.arn
}
