terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}


# remote state backend

# 1. Terraform State 저장을 위한 S3 버킷 생성
resource "aws_s3_bucket" "terraform_state" {
  bucket = "muroom-terraform-state-backend"

  lifecycle {
    prevent_destroy = false #TODO: 운영 시 true로 변경
  }

  force_destroy = true #TODO: 운영 시 false로 변경

  tags = {
    Name = "muroom-terraform-state-backend"
  }
}

# 2. S3 버킷 퍼블릭 액세스 차단 설정 (aws_s3_bucket_public_access_block 리소스를 사용)
resource "aws_s3_bucket_public_access_block" "terraform_state_access_block" {
  bucket                  = aws_s3_bucket.terraform_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 3. S3 버킷 서버 사이드 암호화 설정 (aws_s3_bucket_server_side_encryption_configuration 리소스를 사용)
# 민감한 인프라 상태 정보 보호 목적
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state_encryption" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# 4. S3 버킷 버전 관리 활성화 (aws_s3_bucket_versioning 리소스를 사용)
# 인프라 상태 변경 이력 관리 목적
resource "aws_s3_bucket_versioning" "terraform_state_versioning" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# 5. DynamoDB 테이블 생성 (잠금 및 동시성 제어 목적)
resource "aws_dynamodb_table" "terraform_state_lock" {
  name                        = "muroom-terraform-state-lock"
  billing_mode                = "PAY_PER_REQUEST"
  hash_key                    = "LockID"
  deletion_protection_enabled = true

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name = "muroom-terraform-state-lock"
  }
}
