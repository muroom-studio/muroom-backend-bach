# --------------------------------------------------------
# ECS 클러스터에서 태스크 정의를 사용하여 prod 환경 ECS 서비스 생성
# --------------------------------------------------------
resource "aws_ecs_service" "muroom_backend_bach_service_prod" {
  name                    = "muroom-backend-bach-service-prod"
  cluster                 = aws_ecs_cluster.muroom_ecs_prod_cluster.id
  task_definition         = aws_ecs_task_definition.muroom_ecs_task_definition_prod.arn
  desired_count           = 1
  enable_ecs_managed_tags = true
  propagate_tags          = "TASK_DEFINITION"

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.muroom_ecs_prod_cp.name
    weight            = 1
    base              = 0
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  network_configuration {
    subnets          = [aws_subnet.muroom_private_subnet_2a.id, aws_subnet.muroom_private_subnet_2b.id]
    security_groups  = [aws_security_group.muroom_ecs_instance_prod_sg.id]
    assign_public_ip = false
  }

  placement_constraints {
    type = "distinctInstance"
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.muroom_alb_prod_tg.arn
    container_name   = "muroom-backend-bach-container-prod"
    container_port   = 8080
  }

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  health_check_grace_period_seconds  = 180
  depends_on = [
    aws_lb_listener.muroom_alb_https_listener,
    aws_iam_role_policy_attachment.muroom_prod_task_execution_secretsmanager_policy_attachment
  ]

  tags = { Name = "muroom-backend-bach-service-prod" }
}


# --------------------------------------------------------
# ECS 클러스터에서 태스크 정의를 사용하여 dev 환경 ECS 서비스 생성
# --------------------------------------------------------
resource "aws_ecs_service" "muroom_backend_bach_service_dev" {
  name                    = "muroom-backend-bach-service-dev"
  cluster                 = aws_ecs_cluster.muroom_ecs_dev_cluster.id
  task_definition         = aws_ecs_task_definition.muroom_ecs_task_definition_dev.arn
  desired_count           = 1
  enable_ecs_managed_tags = true
  propagate_tags          = "TASK_DEFINITION"

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.muroom_ecs_dev_cp.name
    weight            = 1
    base              = 0
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  network_configuration {
    subnets          = [aws_subnet.muroom_private_subnet_2a.id]
    security_groups  = [aws_security_group.muroom_ecs_instance_dev_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.muroom_alb_dev_tg.arn
    container_name   = "muroom-backend-bach-container-dev"
    container_port   = 8080
  }

  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100
  health_check_grace_period_seconds  = 180
  depends_on                         = [aws_lb_listener_rule.dev_host_rule]

  tags = { Name = "muroom-backend-bach-service-dev" }
}
