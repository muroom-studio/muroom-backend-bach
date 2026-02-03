# --------------------------------------------------------
# ECS 클러스터 정의
# --------------------------------------------------------
resource "aws_ecs_cluster" "muroom_ecs_prod_cluster" {
  name = "muroom-ecs-prod-cluster"
  tags = { Name = "muroom-ecs-prod-cluster" }
}
resource "aws_ecs_cluster" "muroom_ecs_dev_cluster" {
  name = "muroom-ecs-dev-cluster"
  tags = { Name = "muroom-ecs-dev-cluster" }
}

# --------------------------------------------------------
# ECS Launch Template 정의 (운영용: t4g.medium, 개발용: t4g.small)
# --------------------------------------------------------
resource "aws_launch_template" "muroom_ecs_prod_launch_template" {
  name_prefix            = "muroom-ecs-prod-launch-template-"
  image_id               = data.aws_ami.ecs_optimized_al2023.id
  instance_type          = "t4g.medium"
  vpc_security_group_ids = [aws_security_group.muroom_ecs_instance_prod_sg.id]
  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ecs_prod_instance_profile.name
  }

  lifecycle {
    # ignore_changes = [user_data, image_id] # 배포 환경에 반영하려면, terraform apply -replace="aws_launch_template.muroom_ecs_dev_launch_template"
  }

  user_data = base64encode(<<-EOF
      #!/bin/bash
      # 1. 설정 디렉토리가 없을 경우를 대비
      mkdir -p /etc/ecs

      # 2. 클러스터 이름을 설정 파일에 기록
      echo "ECS_CLUSTER=${aws_ecs_cluster.muroom_ecs_prod_cluster.name}" > /etc/ecs/ecs.config

      # 3. 메타데이터 활성화 설정 추가
      echo "ECS_ENABLE_CONTAINER_METADATA=true" >> /etc/ecs/ecs.config

      # 4. ssm-user에게 docker 권한 부여
      id -u ssm-user &>/dev/null || useradd -m ssm-user
      usermod -a -G docker ssm-user
    EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags          = { Name = "muroom-ecs-prod-node" }
  }
}
resource "aws_launch_template" "muroom_ecs_dev_launch_template" {
  name_prefix            = "muroom-ecs-dev-launch-template-"
  image_id               = data.aws_ami.ecs_optimized_al2023.id
  instance_type          = "t4g.small"
  vpc_security_group_ids = [aws_security_group.muroom_ecs_instance_dev_sg.id]
  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ecs_dev_instance_profile.name
  }

  lifecycle {
    # ignore_changes = [user_data, image_id]
  }

  user_data = base64encode(<<-EOF
      #!/bin/bash
      # 1. 설정 디렉토리가 없을 경우를 대비
      mkdir -p /etc/ecs

      # 2. 클러스터 이름을 설정 파일에 기록
      echo "ECS_CLUSTER=${aws_ecs_cluster.muroom_ecs_dev_cluster.name}" > /etc/ecs/ecs.config

      # 3. 메타데이터 활성화 설정 추가
      echo "ECS_ENABLE_CONTAINER_METADATA=true" >> /etc/ecs/ecs.config

      # 4. ssm-user에게 docker 권한 부여
      id -u ssm-user &>/dev/null || useradd -m ssm-user
      usermod -a -G docker ssm-user
    EOF
  )


  tag_specifications {
    resource_type = "instance"
    tags          = { Name = "muroom-ecs-dev-node" }
  }
}
