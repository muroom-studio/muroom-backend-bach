# --------------------------------------------------------
# 오토 스케일링 그룹 (ASG) 설정
# --------------------------------------------------------
resource "aws_autoscaling_group" "muroom_ecs_prod_asg" {
  name                      = "muroom-ecs-prod-asg"
  vpc_zone_identifier       = [aws_subnet.muroom_private_subnet_2a.id, aws_subnet.muroom_private_subnet_2b.id]
  min_size                  = 1 # 최소 인스턴스 수 (1개에 장애 발생 시 대비)
  max_size                  = 5 # 최대 인스턴스 수
  desired_capacity          = 1 # 시작 시 원하는 인스턴스 수
  wait_for_capacity_timeout = "0"
  health_check_type         = "EC2"
  termination_policies      = ["Default"]
  protect_from_scale_in     = true
  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50 # 최소 50%의 인스턴스가 항상 건강 상태를 유지하도록 함
    }
  }

  launch_template {
    id      = aws_launch_template.muroom_ecs_prod_launch_template.id
    version = "$Latest" # 항상 최신 버전의 런치 템플릿 사용
  }

  lifecycle {
    ignore_changes = [desired_capacity] # desired_capacity 변경 무시 -> Capacity Provider가 관리하도록 함
  }

  # ECS 관리형 ASG 태그
  tag {
    key                 = "AmazonECSManaged"
    value               = ""
    propagate_at_launch = true # EC2 인스턴스에도 태그가 전파되도록 함
  }
  tag {
    key                 = "Name"
    value               = "muroom-ecs-prod-instance"
    propagate_at_launch = true
  }
}
resource "aws_autoscaling_group" "muroom_ecs_dev_asg" {
  name                      = "muroom-ecs-dev-asg"
  vpc_zone_identifier       = [aws_subnet.muroom_private_subnet_2a.id]
  min_size                  = 1
  max_size                  = 1
  desired_capacity          = 1
  wait_for_capacity_timeout = "0"
  health_check_type         = "EC2"
  termination_policies      = ["Default"]
  protect_from_scale_in     = true

  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50
    }
  }

  launch_template {
    id      = aws_launch_template.muroom_ecs_dev_launch_template.id
    version = "$Latest"
  }

  tag {
    key                 = "AmazonECSManaged"
    value               = ""
    propagate_at_launch = true
  }
  tag {
    key                 = "Name"
    value               = "muroom-ecs-dev-instance"
    propagate_at_launch = true
  }
}

# --------------------------------------------------------
# 용량 공급자(Capacity Provider) 설정
# --------------------------------------------------------
resource "aws_ecs_capacity_provider" "muroom_ecs_prod_cp" {
  name = "muroom-ecs-prod-cp"
  auto_scaling_group_provider {
    auto_scaling_group_arn         = aws_autoscaling_group.muroom_ecs_prod_asg.arn
    managed_termination_protection = "ENABLED" # 인스턴스가 불필요하게 종료되는 것을 방지

    managed_scaling {
      status                    = "ENABLED"
      target_capacity           = 100
      minimum_scaling_step_size = 1
      maximum_scaling_step_size = 1
    }
  }
}
resource "aws_ecs_cluster_capacity_providers" "muroom_ecs_prod_cluster_cp_association" {
  cluster_name       = aws_ecs_cluster.muroom_ecs_prod_cluster.name
  capacity_providers = [aws_ecs_capacity_provider.muroom_ecs_prod_cp.name]
  default_capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.muroom_ecs_prod_cp.name
    base              = 0
    weight            = 1
  }
}

resource "aws_ecs_capacity_provider" "muroom_ecs_dev_cp" {
  name = "muroom-ecs-dev-cp"
  auto_scaling_group_provider {
    auto_scaling_group_arn         = aws_autoscaling_group.muroom_ecs_dev_asg.arn
    managed_termination_protection = "DISABLED"

    managed_scaling {
      status                    = "ENABLED"
      target_capacity           = 100
      minimum_scaling_step_size = 1
      maximum_scaling_step_size = 1
    }
  }
}

resource "aws_ecs_cluster_capacity_providers" "muroom_ecs_dev_cluster_cp_association" {
  cluster_name       = aws_ecs_cluster.muroom_ecs_dev_cluster.name
  capacity_providers = [aws_ecs_capacity_provider.muroom_ecs_dev_cp.name]
  default_capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.muroom_ecs_dev_cp.name
    base              = 0
    weight            = 1
  }
}