# --------------------------------------------------------
# prod 환경의 Valkey 인스턴스 및 스토리지 생성
# --------------------------------------------------------
resource "aws_launch_template" "muroom_valkey_prod_launch_template" {
  name_prefix   = "muroom-valkey-prod-launch-template-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.micro"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_valkey_prod_instance_profile.name
  }

  lifecycle {
    ignore_changes = [
      image_id,
    ]
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 20
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/shell-script/valkey_user_data.sh.tpl", {
    ebs_volume_id         = aws_ebs_volume.muroom_valkey_prod_storage.id
    mount_point           = var.valkey_ebs_mount_point
    valkey_version        = "8.1.5"
    valkey_secret_arn     = aws_secretsmanager_secret.muroom_valkey_prod_secret_manager.arn
    aws_region            = data.aws_region.current.id
    backup_s3_bucket_name = aws_s3_bucket.muroom_prod_valkey_backup.id
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-valkey-prod-instance"
    }
  }
}
resource "aws_ebs_volume" "muroom_valkey_prod_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 10
  type              = "gp3"
  encrypted         = true

  lifecycle {
    prevent_destroy = false
  }

  tags = {
    Name        = "muroom-valkey-prod-storage"
    Snapshot    = "true"
    Environment = "prod"
  }
}
resource "aws_instance" "muroom_valkey_prod_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_valkey_prod_sg.id]

  lifecycle {
    ignore_changes  = [ami, user_data, launch_template]
    prevent_destroy = false
  }

  launch_template {
    id = aws_launch_template.muroom_valkey_prod_launch_template.id
    # version = "$Latest"
  }

  depends_on = [aws_route.muroom_private_route_via_nat_instance]

  tags = {
    Name = "muroom-valkey-prod-instance"
  }
}
resource "aws_volume_attachment" "muroom_valkey_prod_storage_attachment" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.muroom_valkey_prod_storage.id
  instance_id = aws_instance.muroom_valkey_prod_instance.id
}

# --------------------------------------------------------
# dev 환경의 Valkey 인스턴스 및 스토리지 생성
# --------------------------------------------------------
resource "aws_launch_template" "muroom_valkey_dev_launch_template" {
  name_prefix   = "muroom-valkey-dev-launch-template-"
  image_id      = data.aws_ami.amazon_linux_2023.id
  instance_type = "t4g.nano"

  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_valkey_dev_instance_profile.name
  }

  lifecycle {
    ignore_changes = [
      image_id,
    ]
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 10
      volume_type = "gp3"
      encrypted   = true
    }
  }

  user_data = base64encode(templatefile("${path.module}/shell-script/valkey_user_data.sh.tpl", {
    ebs_volume_id         = aws_ebs_volume.muroom_valkey_dev_storage.id
    mount_point           = var.valkey_ebs_mount_point
    valkey_version        = "8.1.5"
    valkey_secret_arn     = aws_secretsmanager_secret.muroom_valkey_dev_secret_manager.arn
    aws_region            = data.aws_region.current.id
    backup_s3_bucket_name = aws_s3_bucket.muroom_dev_valkey_backup.id
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "muroom-valkey-dev-instance"
    }
  }
}
resource "aws_ebs_volume" "muroom_valkey_dev_storage" {
  availability_zone = aws_subnet.muroom_private_subnet_2a.availability_zone
  size              = 1
  type              = "gp3"
  encrypted         = true

  lifecycle {
    prevent_destroy = false
  }

  tags = {
    Name        = "muroom-valkey-dev-storage"
    Snapshot    = "true"
    Environment = "dev"
  }
}
resource "aws_instance" "muroom_valkey_dev_instance" {
  subnet_id              = aws_subnet.muroom_private_subnet_2a.id
  vpc_security_group_ids = [aws_security_group.muroom_valkey_dev_sg.id]

  lifecycle {
    ignore_changes  = [ami, user_data, launch_template]
    prevent_destroy = false
  }

  launch_template {
    id = aws_launch_template.muroom_valkey_dev_launch_template.id
    # version = "$Latest"
  }

  depends_on = [aws_route.muroom_private_route_via_nat_instance]

  tags = {
    Name = "muroom-valkey-dev-instance"
  }
}
resource "aws_volume_attachment" "muroom_valkey_dev_storage_attachment" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.muroom_valkey_dev_storage.id
  instance_id = aws_instance.muroom_valkey_dev_instance.id
}
