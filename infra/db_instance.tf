# --------------------------------------------------------
# Self-Hosted prod DB 인스턴스 launch template 및 스토리지 설정
# --------------------------------------------------------
resource "aws_launch_template" "muroom_db_prod_launch_template" {
  name_prefix   = "muroom-db-prod-"
  name          = "muroom-db-prod-launch-template"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.small"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ssm_instance_profile.name
  }

  user_data = base64encode(templatefile("${path.module}/shell-script/muroom_db_user_data.sh.tpl", {
    ebs_volume_id = aws_ebs_volume.muroom_db_prod_storage.id
    mount_point   = var.db_ebs_mount_point
    pg_version    = "17"
    db_secret_arn = aws_secretsmanager_secret.db_prod_secret_manager.arn
    aws_region    = data.aws_region.current.name
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-db-prod-launch-template"
    }
  }
}
resource "aws_ebs_volume" "muroom_db_prod_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 100
  type              = "gp3"
  encrypted         = true

  lifecycle {
    prevent_destroy = false #TODO: 운영 시 true로 변경
  }

  tags = {
    Name        = "muroom-db-prod-storage"
    Snapshot    = "true"
    Environment = "prod"
  }
}
resource "aws_instance" "muroom_db_prod_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_db_prod_sg.id]

  launch_template {
    id      = aws_launch_template.muroom_db_prod_launch_template.id
    version = "$Latest"
  }

  tags = {
    Name = "muroom-db-prod-instance"
  }
}
resource "aws_volume_attachment" "muroom_db_prod_storage_attachment" {
  device_name = "/dev/sdf"
  instance_id = aws_instance.muroom_db_prod_instance.id
  volume_id   = aws_ebs_volume.muroom_db_prod_storage.id
}
# --- DLM을 사용한 EBS 스냅샷 백업 정책 설정 ---
resource "aws_dlm_lifecycle_policy" "muroom_db_prod_backup_policy" {
  description        = "운영 DB 볼륨 스냅샷 백업 정책"
  execution_role_arn = aws_iam_role.muroom_dlm_role.arn
  state              = "ENABLED"

  policy_details {
    resource_types = ["VOLUME"]
    target_tags = {
      Snapshot    = "true"
      Environment = "prod"
    }

    # 일일 백업 스케줄 설정
    schedule {
      name        = "DatabaseDailyBackupSnapshots"
      tags_to_add = { "SnapshotCreator" = "DLM" }
      copy_tags   = true
      create_rule {
        interval      = 24
        interval_unit = "HOURS"
        times         = ["18:00"] #한국 시간 기준 매일 새벽 3시에 백업 생성
      }
      retain_rule {
        count = 14
      }
      cross_region_copy_rule {
        target    = "ap-southeast-1" # 싱가포르 리전
        encrypted = true
        copy_tags = true
        retain_rule {
          interval      = 14
          interval_unit = "DAYS"
        }
      }
    }


    # 주간 백업 스케줄 설정
    schedule {
      name        = "DatabaseWeeklyBackupSnapshots"
      tags_to_add = { "SnapshotCreator" = "DLM", Tier = "Weekly" }
      copy_tags   = true
      create_rule {
        cron_expression = "cron(0 19 * ? *)" #한국 시간 기준 매주 일요일 새벽 4시에 백업 생성
      }
      retain_rule {
        count = 12
      }
    }

    # 월간 백업 스케줄 설정
    schedule {
      name        = "DatabaseMonthlyBackupSnapshots"
      tags_to_add = { "SnapshotCreator" = "DLM", Tier = "Monthly" }
      copy_tags   = true
      create_rule {
        cron_expression = "cron(0 20 1 * ? *)" #한국 시간 기준 매월 1일 새벽 5시에 백업 생성
      }
      retain_rule {
        count = 12
      }
    }
  }
}

# --------------------------------------------------------
# Self-Hosted dev DB 인스턴스 launch template 및 스토리지 설정
# --------------------------------------------------------
resource "aws_launch_template" "muroom_db_dev_launch_template" {
  name_prefix   = "muroom-db-dev-"
  name          = "muroom-db-dev-launch-template"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.nano"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ssm_instance_profile.name
  }

  user_data = templatefile("${path.module}/shell-script/muroom_db_user_data.sh.tpl", {
    ebs_volume_id = aws_ebs_volume.muroom_db_dev_storage.id
    mount_point   = var.db_ebs_mount_point
    pg_version    = "17"
    db_secret_arn = aws_secretsmanager_secret.db_dev_secret_manager.arn
    aws_region    = data.aws_region.current.name
  })

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-db-dev-launch-template"
    }
  }
}
resource "aws_ebs_volume" "muroom_db_dev_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 10
  type              = "gp3"
  encrypted         = true

  lifecycle {
    prevent_destroy = false #TODO: 운영 시 true로 변경
  }

  tags = {
    Name        = "muroom-db-dev-storage"
    Snapshot    = "true"
    Environment = "dev"
  }
}
resource "aws_instance" "muroom_db_dev_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_db_dev_sg.id]

  launch_template {
    id      = aws_launch_template.muroom_db_dev_launch_template.id
    version = "$Latest"
  }

  tags = {
    Name = "muroom-db-dev-instance"
  }
}
resource "aws_volume_attachment" "muroom_db_dev_storage_attachment" {
  device_name = "/dev/sdf"
  instance_id = aws_instance.muroom_db_dev_instance.id
  volume_id   = aws_ebs_volume.muroom_db_dev_storage.id
}
resource "aws_dlm_lifecycle_policy" "muroom_db_dev_backup_policy" {
  description        = "개발 DB 볼륨 스냅샷 백업 정책"
  execution_role_arn = aws_iam_role.muroom_dlm_role.arn
  state              = "ENABLED"

  policy_details {
    resource_types = ["VOLUME"]
    target_tags = {
      Snapshot    = "true"
      Environment = "dev"
    }
    schedule {
      name        = "DatabaseDailyBackupSnapshots"
      tags_to_add = { "SnapshotCreator" = "DLM" }
      copy_tags   = true

      create_rule {
        interval      = 24
        interval_unit = "HOURS"
        times         = ["18:00"]
      }

      retain_rule {
        count = 2
      }
    }
  }
}
