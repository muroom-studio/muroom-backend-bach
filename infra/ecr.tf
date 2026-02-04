# --------------------------------------------------------
# ECR 리포지토리 생성
# --------------------------------------------------------
resource "aws_ecr_repository" "muroom_backend_bach" {
  name                 = "muroom/backend-bach-repository"
  image_tag_mutability = "MUTABLE" # 태그를 덮어쓸 수 있도록 설정 (예: latest 태그)

  image_scanning_configuration {
    scan_on_push = true # 이미지를 푸시할 때마다 취약점 스캔
  }

  lifecycle {
    prevent_destroy = true
  }
  force_delete = false

  tags = {
    Name = "muroom-backend-bach-repository"
  }
}

# --------------------------------------------------------
# ECR 라이프사이클 정책 설정
# --------------------------------------------------------
resource "aws_ecr_lifecycle_policy" "muroom_backend_bach_lifecycle_policy" {
  repository = aws_ecr_repository.muroom_backend_bach.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 10,
        description  = "Prod images expire after keeping only 20 latest tagged images",
        selection = {
          tagStatus     = "tagged",
          tagPrefixList = ["prod-", "main-", "release-"], # 지정된 태그 프리픽스 이미지
          countType     = "imageCountMoreThan",           # 20개 초과 시
          countNumber   = 20                              # 20개만 남기고 삭제
        },
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 20,
        description  = "Dev images expire after 14 days",
        selection = {
          tagStatus     = "tagged",
          tagPrefixList = ["dev-", "feat-"],  # 지정된 태그 프리픽스 이미지
          countType     = "sinceImagePushed", # 푸시된 시점부터
          countUnit     = "days",             # 일수
          countNumber   = 14                  # 14일 초과 시 삭제
        },
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 30,
        description  = "Untagged images expire after 7 days",
        selection = {
          tagStatus   = "untagged", # 태그가 없는 이미지
          countType   = "sinceImagePushed",
          countUnit   = "days",
          countNumber = 7 # 7일 초과 시 삭제
        },
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 100,
        description  = "Expire images to limit total image count to 100",
        selection = {
          tagStatus   = "any",                # 모든 이미지 대상
          countType   = "imageCountMoreThan", # 100개 초과 시
          countNumber = 100                   # 100개만 남기고 삭제
        },
        action = {
          type = "expire"
        }
      }
    ]
  })
}
