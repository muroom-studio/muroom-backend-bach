# CloudFront 뒤에 위치할 Application Load Balancer 생성
resource "aws_lb" "muroom_alb" {
  name               = "muroom-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.muroom_alb_sg.id]
  subnets = [
    aws_subnet.muroom_public_subnet_2a.id,
    aws_subnet.muroom_public_subnet_2b.id
  ]

  enable_deletion_protection = true

  tags = {
    Name = "muroom-alb"
  }
}

resource "aws_lb_target_group" "muroom_alb_prod_tg" {
  name        = "muroom-alb-prod-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.muroom_vpc.id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  health_check {
    path                = "/actuator/health/readiness"
    protocol            = "HTTP"
    port                = "8080"
    matcher             = "200-399"
    interval            = 30
    timeout             = 10
    healthy_threshold   = 3
    unhealthy_threshold = 3
  }

  tags = {
    Name = "muroom-alb-prod-tg"
  }
}
resource "aws_lb_target_group" "muroom_alb_dev_tg" {
  name        = "muroom-alb-dev-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.muroom_vpc.id
  target_type = "ip"

  lifecycle {
    create_before_destroy = true
  }

  health_check {
    path                = "/actuator/health/readiness"
    protocol            = "HTTP"
    port                = "8080"
    matcher             = "200-399"
    interval            = 30
    timeout             = 10
    healthy_threshold   = 3
    unhealthy_threshold = 3
  }

  tags = {
    Name = "muroom-alb-dev-tg"
  }
}

resource "aws_lb_listener" "muroom_alb_https_listener" {
  load_balancer_arn = aws_lb.muroom_alb.arn
  port              = 443
  protocol          = "HTTPS"

  certificate_arn = aws_acm_certificate_validation.cert_validation.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.muroom_alb_prod_tg.arn
  }

  tags = {
    Name = "muroom-alb-https-listener"
  }
}

# Host 헤더(도메인 이름)가 'dev-api.muroom.kr'인 요청은 개발(Dev) 타겟 그룹으로 포워딩
resource "aws_lb_listener_rule" "dev_host_rule" {
  listener_arn = aws_lb_listener.muroom_alb_https_listener.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.muroom_alb_dev_tg.arn
  }

  condition {
    host_header {
      values = ["dev-api.muroom.kr"]
    }
  }
}
