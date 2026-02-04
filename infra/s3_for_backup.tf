# ----------------------------------------------------
# DB 백업용 S3 버킷
# ----------------------------------------------------
resource "aws_s3_bucket" "muroom_prod_postgres_backup" {
  bucket = "muroom-prod-postgres-backup"
  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}

# 보안 설정 (비공개)
resource "aws_s3_bucket_public_access_block" "prod_postrges_backup_access_block" {
  bucket                  = aws_s3_bucket.muroom_prod_postgres_backup.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 라이프사이클 설정 (백업 비용 최적화)
resource "aws_s3_bucket_lifecycle_configuration" "prod_postgres_backup_lifecycle" {
  bucket = aws_s3_bucket.muroom_prod_postgres_backup.id

  # --- 규칙 1: 실시간 WAL 로그 전용 (짧고 굵게 보관) ---
  rule {
    id     = "postgres-wal-optimization"
    status = "Enabled"

    filter {
      prefix = "backups/postgres/wal/"
    }

    # WAL은 최근 3일치만 있으면 충분합니다. (3일 전 덤프와 조합 가능)
    # 3일이 지나면 바로 삭제하여 비용을 최소화합니다.
    expiration {
      days = 3
    }
  }

  # --- 규칙 2: 전체 덤프 전용 (기존 설정 유지 및 경로 구체화) ---
  rule {
    id     = "prod-postgres-dump-optimization"
    status = "Enabled"

    filter {
      # 덤프가 저장되는 날짜별 폴더들이 'backups/postgres/2026...' 형식이므로 prefix 지정
      prefix = "backups/postgres/20"
    }

    # 30일 후 저렴한 저장소로 이동
    transition {
      days          = 30
      storage_class = "GLACIER_IR"
    }

    # 60일 후 최종 삭제
    expiration {
      days = 60
    }
  }
}

# 5-1-4. 개발(Dev) DB 백업용 버킷
resource "aws_s3_bucket" "muroom_dev_postgres_backup" {
  bucket = "muroom-dev-postgres-backup"
  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}

resource "aws_s3_bucket_lifecycle_configuration" "dev_postgres_backup_lifecycle" {
  bucket = aws_s3_bucket.muroom_dev_postgres_backup.id

  # 1. WAL 로그는 무조건 7일 삭제 (용량 방지)
  rule {
    id     = "dev-postgres-wal-cleanup"
    status = "Enabled"
    filter {
      prefix = "backups/postgres/wal/"
    }
    expiration {
      days = 7
    }
  }

  # 2. 전체 덤프는 14일 정도 보관 (개발용 시나리오)
  rule {
    id     = "dev-postgres-dump-cleanup"
    status = "Enabled"
    filter {
      prefix = "backups/postgres/20"
    }
    expiration {
      days = 14
    }
  }
}

# ----------------------------------------------------
# Valkey 백업용 S3 버킷
# ----------------------------------------------------
resource "aws_s3_bucket" "muroom_prod_valkey_backup" {
  bucket = "muroom-prod-valkey-backup"
  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}
resource "aws_s3_bucket_public_access_block" "prod_valkey_backup_access_block" {
  bucket                  = aws_s3_bucket.muroom_prod_valkey_backup.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_lifecycle_configuration" "prod_valkey_backup_lifecycle" {
  bucket = aws_s3_bucket.muroom_prod_valkey_backup.id

  rule {
    id     = "prod-valkey-backup-optimization"
    status = "Enabled"

    expiration {
      days = 14
    }
  }
}

resource "aws_s3_bucket" "muroom_dev_valkey_backup" {
  bucket = "muroom-dev-valkey-backup"
  lifecycle {
    prevent_destroy = true
  }
  force_destroy = false
}
resource "aws_s3_bucket_lifecycle_configuration" "dev_valkey_backup_lifecycle" {
  bucket = aws_s3_bucket.muroom_dev_valkey_backup.id

  rule {
    id     = "dev-valkey-backup-retention"
    status = "Enabled"

    expiration {
      days = 7
    }
  }
}
