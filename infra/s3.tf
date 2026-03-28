# ------------------------------------------------
# S3 버킷 설정
# ------------------------------------------------
variable "prod_s3_private_bucket_name" {
  type    = string
  default = "muroom-prod-private-storage"
}
variable "prod_s3_public_bucket_name" {
  type    = string
  default = "muroom-prod-public-storage"
}
variable "dev_s3_private_bucket_name" {
  type    = string
  default = "muroom-dev-private-storage"
}
variable "dev_s3_public_bucket_name" {
  type    = string
  default = "muroom-dev-public-storage"
}

# ------------------------------------------------
# Private Buckets (비공개 컨텐츠용, 운영용 및 개발용)
# ------------------------------------------------
resource "aws_s3_bucket" "muroom_prod_private_storage" {
  bucket = var.prod_s3_private_bucket_name

  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}
resource "aws_s3_bucket_versioning" "prod_private_versioning" {
  bucket = aws_s3_bucket.muroom_prod_private_storage.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_server_side_encryption_configuration" "prod_private_encryption" {
  bucket = aws_s3_bucket.muroom_prod_private_storage.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
resource "aws_s3_bucket_public_access_block" "prod_private_access_block" {
  bucket = aws_s3_bucket.muroom_prod_private_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_lifecycle_configuration" "prod_private_lifecycle" {
  bucket = aws_s3_bucket.muroom_prod_private_storage.id

  rule {
    id     = "prod-private-version-management"
    status = "Enabled"

    # 1. 이전 버전(non-current)을 Intelligent-Tiering으로 이동하여 자동 비용 최적화
    noncurrent_version_transition {
      noncurrent_days = 0
      storage_class   = "INTELLIGENT_TIERING"
    }

    # 2. 아주 오래된 이전 버전은 영구 삭제
    noncurrent_version_expiration {
      noncurrent_days = 90
    }

    # 3. 업로드 실패 조각 삭제
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "prod-private-temp-cleanup"
    status = "Enabled"

    filter {
      prefix = "temp/" # temp/ 폴더 내의 객체에만 적용
    }

    expiration {
      days = 7
    }
  }

  rule {
    id     = "prod-private-deletion-scheduled-cleanup"
    status = "Enabled"

    filter {
      prefix = "deletion-scheduled/" # deletion-scheduled/ 폴더 내의 객체에만 적용
    }

    expiration {
      days = 7
    }
  }

  rule {
    id     = "prod-private-draft-cleanup"
    status = "Enabled"

    filter {
      prefix = "draft/" # draft/ 폴더 내의 고아 객체 자동 정리
    }

    expiration {
      days = 7
    }
  }
}

resource "aws_s3_bucket" "muroom_dev_private_storage" {
  bucket = var.dev_s3_private_bucket_name

  lifecycle {
    prevent_destroy = true
  }

  force_destroy = false
}
resource "aws_s3_bucket_versioning" "dev_private_versioning" {
  bucket = aws_s3_bucket.muroom_dev_private_storage.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_server_side_encryption_configuration" "dev_private_encryption" {
  bucket = aws_s3_bucket.muroom_dev_private_storage.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
  }
}
resource "aws_s3_bucket_public_access_block" "dev_private_access_block" {
  bucket = aws_s3_bucket.muroom_dev_private_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_lifecycle_configuration" "dev_private_lifecycle" {
  bucket = aws_s3_bucket.muroom_dev_private_storage.id

  rule {
    id     = "dev-private-version-management"
    status = "Enabled"

    noncurrent_version_expiration { noncurrent_days = 1 }

    abort_incomplete_multipart_upload { days_after_initiation = 1 }
  }

  rule {
    id     = "dev-private-temp-cleanup"
    status = "Enabled"

    filter {
      prefix = "temp/" # temp/ 폴더 내의 객체에만 적용
    }

    expiration {
      days = 1
    }
  }

  rule {
    id     = "dev-private-deletion-scheduled-cleanup"
    status = "Enabled"

    filter {
      prefix = "deletion-scheduled/"
    }

    expiration {
      days = 1
    }
  }

  rule {
    id     = "dev-private-draft-cleanup"
    status = "Enabled"

    filter {
      prefix = "draft/"
    }

    expiration {
      days = 1
    }
  }
}

# ----------------------------------------------------
# Public Buckets (공개 컨텐츠용, 운영용 및 개발용)
resource "aws_s3_bucket" "muroom_prod_public_storage" {
  bucket = var.prod_s3_public_bucket_name

  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}
resource "aws_s3_bucket_public_access_block" "prod_public_access_block" {
  bucket = aws_s3_bucket.muroom_prod_public_storage.id

  block_public_acls       = true
  block_public_policy     = false
  ignore_public_acls      = true
  restrict_public_buckets = false
}
resource "aws_s3_bucket_policy" "prod_public_policy" {
  bucket = aws_s3_bucket.muroom_prod_public_storage.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "AllowPublicRead"
        Effect    = "Allow"
        Principal = "*"
        Action    = ["s3:GetObject"]
        Resource  = "${aws_s3_bucket.muroom_prod_public_storage.arn}/*"
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.prod_public_access_block]
}
resource "aws_s3_bucket_versioning" "prod_public_versioning" {
  bucket = aws_s3_bucket.muroom_prod_public_storage.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_lifecycle_configuration" "prod_public_lifecycle" {
  bucket = aws_s3_bucket.muroom_prod_public_storage.id

  rule {
    id     = "prod-public-version-management"
    status = "Enabled"
    noncurrent_version_expiration { noncurrent_days = 90 }
    abort_incomplete_multipart_upload { days_after_initiation = 7 }
  }

  rule {
    id     = "prod-public-staging-cleanup"
    status = "Enabled"

    filter {
      prefix = "temp/"
    }

    # 3일이 지난 임시 파일은 영구 삭제
    expiration {
      days = 7
    }

    # 업로드 실패 조각은 7일 후 삭제
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "prod-public-deletion-scheduled-cleanup"
    status = "Enabled"

    filter {
      prefix = "deletion-scheduled/"
    }

    expiration {
      days = 7
    }
  }
}

resource "aws_s3_bucket" "muroom_dev_public_storage" {
  bucket = var.dev_s3_public_bucket_name

  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}
resource "aws_s3_bucket_public_access_block" "dev_public_access_block" {
  bucket = aws_s3_bucket.muroom_dev_public_storage.id

  block_public_acls       = true
  block_public_policy     = false
  ignore_public_acls      = true
  restrict_public_buckets = false
}
resource "aws_s3_bucket_policy" "dev_public_policy" {
  bucket = aws_s3_bucket.muroom_dev_public_storage.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "AllowPublicRead"
        Effect    = "Allow"
        Principal = "*"
        Action    = ["s3:GetObject"]
        Resource  = "${aws_s3_bucket.muroom_dev_public_storage.arn}/*"
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.dev_public_access_block]
}
resource "aws_s3_bucket_versioning" "dev_public_versioning" {
  bucket = aws_s3_bucket.muroom_dev_public_storage.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_lifecycle_configuration" "dev_public_lifecycle" {
  bucket = aws_s3_bucket.muroom_dev_public_storage.id
  rule {
    id     = "dev-public-version-management"
    status = "Enabled"
    noncurrent_version_expiration { noncurrent_days = 1 }
    abort_incomplete_multipart_upload { days_after_initiation = 1 }
  }

  rule {
    id     = "dev-public-staging-cleanup"
    status = "Enabled"

    filter {
      prefix = "temp/"
    }

    expiration {
      days = 2
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }

  rule {
    id     = "dev-public-deletion-scheduled-cleanup"
    status = "Enabled"

    filter {
      prefix = "deletion-scheduled/"
    }

    expiration {
      days = 1
    }
  }
}

locals {
  s3_cors_allowed_origins_prod = [
    "https://muroom.kr",
    "https://admin.muroom.kr",
  ]
  s3_cors_allowed_origins_dev = [
    "https://dev.muroom.kr",
    "https://dev.admin.muroom.kr",
    "http://localhost:3000"
  ]
}
resource "aws_s3_bucket_cors_configuration" "prod_public_cors" {
  bucket = aws_s3_bucket.muroom_prod_public_storage.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = local.s3_cors_allowed_origins_prod
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}
resource "aws_s3_bucket_cors_configuration" "dev_public_cors" {
  bucket = aws_s3_bucket.muroom_dev_public_storage.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = local.s3_cors_allowed_origins_dev
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}
resource "aws_s3_bucket_cors_configuration" "prod_private_cors" {
  bucket = aws_s3_bucket.muroom_prod_private_storage.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = local.s3_cors_allowed_origins_prod
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}
resource "aws_s3_bucket_cors_configuration" "dev_private_cors" {
  bucket = aws_s3_bucket.muroom_dev_private_storage.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = local.s3_cors_allowed_origins_dev
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

