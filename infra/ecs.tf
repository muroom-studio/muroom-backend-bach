# --------------------------------------------------------
data "aws_ami" "ecs_optimized_al2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-ecs-hvm-*-arm64-ebs"] # Amazon Linux 2 ARM64 ECS 최적화 AMI 패턴
  }
  filter {
    name   = "architecture"
    values = ["arm64"] # ARM64 아키텍처 지정
  }
}

# --------------------------------------------------------
# ECS 클러스터 prod 환경 설정
# --------------------------------------------------------
resource "aws_ecs_cluster" "muroom_ecs_prod_cluster" {
  name = "muroom-ecs-prod-cluster"
  tags = { Name = "muroom-ecs-prod-cluster" }
}

resource "aws_launch_template" "muroom_ecs_prod_launch_template" {
  name                   = "muroom-ecs-prod-launch-template"
  image_id               = data.aws_ami.ecs_optimized_al2.id
  instance_type          = "t4g.medium"
  vpc_security_group_ids = [aws_security_group.muroom_ecs_instance_prod_sg.id]
  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ecs_instance_profile.name
  }

  user_data = base64encode(
    # ECS Agent 설정: 어떤 클러스터에 인스턴스를 등록할지 지정
    # ECS_ENABLE_CONTAINER_METADATA는 컨테이너 메타데이터를 활성화하여 모니터링 및 로깅에 유용
    <<EOF
      #!/bin/bash
      echo ECS_CLUSTER=${aws_ecs_cluster.muroom_ecs_prod_cluster.name} >> /etc/ecs/ecs.config
      echo ECS_ENABLE_CONTAINER_METADATA=true >> /etc/ecs/ecs.config
    EOF
  )

  tags = { Name = "muroom-ecs-prod-launch-template" }
}

# --------------------------------------------------------
# ECS 클러스터 dev 환경 설정
# --------------------------------------------------------
resource "aws_ecs_cluster" "muroom_ecs_dev_cluster" {
  name = "muroom-ecs-dev-cluster"
  tags = { Name = "muroom-ecs-dev-cluster" }
}

resource "aws_launch_template" "muroom_ecs_dev_launch_template" {
  name                   = "muroom-ecs-dev-launch-template"
  image_id               = data.aws_ami.ecs_optimized_al2.id
  instance_type          = "t4g.small"
  vpc_security_group_ids = [aws_security_group.muroom_ecs_instance_dev_sg.id]
  iam_instance_profile {
    name = aws_iam_instance_profile.muroom_ecs_instance_profile.name
  }

  user_data = base64encode(
    <<EOF
      #!/bin/bash
      echo ECS_CLUSTER=${aws_ecs_cluster.muroom_ecs_dev_cluster.name} >> /etc/ecs/ecs.config
      echo ECS_ENABLE_CONTAINER_METADATA=true >> /etc/ecs/ecs.config
    EOF
  )

  tags = { Name = "muroom-ecs-dev-launch-template" }
}
