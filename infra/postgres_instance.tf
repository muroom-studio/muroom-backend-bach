# --------------------------------------------------------
# Self-Hosted prod Postgres 인스턴스 launch template 및 스토리지 설정
# --------------------------------------------------------
resource "aws_launch_template" "muroom_postgres_prod_launch_template" {
  name_prefix   = "muroom-postgres-prod-launch-template-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.small"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_postgres_prod_instance_profile.name
  }

  lifecycle {
    # ignore_changes = [image_id]
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 20
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/shell-script/postgres_user_data.sh.tpl", {
    ebs_volume_id         = aws_ebs_volume.muroom_postgres_prod_storage.id
    mount_point           = var.postgres_ebs_mount_point
    postgres_secret_arn   = aws_secretsmanager_secret.muroom_postgres_prod_secret_manager.arn
    aws_region            = data.aws_region.current.id
    backup_s3_bucket_name = aws_s3_bucket.muroom_prod_postgres_backup.id
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-postgres-prod-instance"
    }
  }
}
resource "aws_ebs_volume" "muroom_postgres_prod_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 30
  type              = "gp3"
  iops              = 3000
  throughput        = 125
  encrypted         = true

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name        = "muroom-postgres-prod-storage"
    Snapshot    = "true"
    Environment = "prod"
  }
}
resource "aws_instance" "muroom_postgres_prod_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_postgres_prod_sg.id]

  lifecycle {
    # ignore_changes  = [ami, user_data, launch_template]
    prevent_destroy = true
  }

  launch_template {
    id = aws_launch_template.muroom_postgres_prod_launch_template.id
    # version = "$Latest"
  }

  depends_on = [aws_route.muroom_private_route_via_nat_instance]

  tags = {
    Name = "muroom-postgres-prod-instance"
  }
}
resource "aws_volume_attachment" "muroom_postgres_prod_storage_attachment" {
  device_name = "/dev/sdf"
  instance_id = aws_instance.muroom_postgres_prod_instance.id
  volume_id   = aws_ebs_volume.muroom_postgres_prod_storage.id
}
# --- DLM을 사용한 EBS 스냅샷 백업 정책 설정 ---
resource "aws_dlm_lifecycle_policy" "muroom_postgres_prod_backup_policy" {
  description        = "Prod Postgres volume snapshot backup policy"
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
        count = 7
      }
      # cross_region_copy_rule {
      #   target    = "ap-southeast-1" # 싱가포르 리전
      #   encrypted = true
      #   copy_tags = true
      #   retain_rule {
      #     interval      = 14
      #     interval_unit = "DAYS"
      #   }
      # }
    }


    # 주간 백업 스케줄 설정
    # schedule {
    #   name        = "DatabaseWeeklyBackupSnapshots"
    #   tags_to_add = { "SnapshotCreator" = "DLM", Tier = "Weekly" }
    #   copy_tags   = true
    #   create_rule {
    #     cron_expression = "cron(0 19 ? * SUN *)" #한국 시간 기준 매주 일요일 새벽 4시에 백업 생성
    #   }
    #   retain_rule {
    #     count = 12
    #   }
    # }

    # 월간 백업 스케줄 설정
    # schedule {
    #   name        = "DatabaseMonthlyBackupSnapshots"
    #   tags_to_add = { "SnapshotCreator" = "DLM", Tier = "Monthly" }
    #   copy_tags   = true
    #   create_rule {
    #     cron_expression = "cron(0 20 1 * ? *)" #한국 시간 기준 매월 1일 새벽 5시에 백업 생성
    #   }
    #   retain_rule {
    #     count = 12
    #   }
    # }
  }
}

# --------------------------------------------------------
# Self-Hosted dev Postgres 인스턴스 launch template 및 스토리지 설정
# --------------------------------------------------------
resource "aws_launch_template" "muroom_postgres_dev_launch_template" {
  name_prefix   = "muroom-postgres-dev-launch-template-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.micro"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_postgres_dev_instance_profile.name
  }

  lifecycle {
    # ignore_changes = [image_id]
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 10
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/shell-script/postgres_user_data.sh.tpl", {
    ebs_volume_id         = aws_ebs_volume.muroom_postgres_dev_storage.id
    mount_point           = var.postgres_ebs_mount_point
    postgres_secret_arn   = aws_secretsmanager_secret.muroom_postgres_dev_secret_manager.arn
    aws_region            = data.aws_region.current.id
    backup_s3_bucket_name = aws_s3_bucket.muroom_dev_postgres_backup.id
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-postgres-dev-instance"
    }
  }
}
resource "aws_ebs_volume" "muroom_postgres_dev_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 10
  type              = "gp3"
  encrypted         = true

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name        = "muroom-postgres-dev-storage"
    Snapshot    = "true"
    Environment = "dev"
  }
}
resource "aws_instance" "muroom_postgres_dev_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_postgres_dev_sg.id]

  lifecycle {
    # ignore_changes  = [ami, user_data, launch_template]
    prevent_destroy = true
  }

  launch_template {
    id = aws_launch_template.muroom_postgres_dev_launch_template.id
    # version = "$Latest"
  }

  depends_on = [aws_route.muroom_private_route_via_nat_instance]

  tags = {
    Name = "muroom-postgres-dev-instance"
  }
}
resource "aws_volume_attachment" "muroom_postgres_dev_storage_attachment" {
  device_name = "/dev/sdf"
  instance_id = aws_instance.muroom_postgres_dev_instance.id
  volume_id   = aws_ebs_volume.muroom_postgres_dev_storage.id
}
resource "aws_dlm_lifecycle_policy" "muroom_postgres_dev_backup_policy" {
  description        = "Dev Postgres volume snapshot backup policy"
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
