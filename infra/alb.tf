# --------------------------------------------------------
# 1. Application Load Balancer 생성
# --------------------------------------------------------
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

# --------------------------------------------------------
# 2. Target Groups (awsvpc 모드이므로 target_type = "ip")
# --------------------------------------------------------
resource "aws_lb_target_group" "muroom_alb_prod_tg" {
  name        = "muroom-alb-prod-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.muroom_vpc.id
  target_type = "ip" # awsvpc 모드

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

# --------------------------------------------------------
# 3. Listeners (HTTP 리다이렉트 & HTTPS 처리)
# --------------------------------------------------------
resource "aws_lb_listener" "muroom_alb_http_redirect" {
  load_balancer_arn = aws_lb.muroom_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}
resource "aws_lb_listener" "muroom_alb_https_listener" {
  load_balancer_arn = aws_lb.muroom_alb.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = data.aws_acm_certificate.prod_cert.arn

  # 기본 액션: 등록되지 않은 도메인이나 IP 접속 시 404 차단 (보안 장치)
  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}
# 개발용 인증서 추가로 붙이기
resource "aws_lb_listener_certificate" "dev_cert_attachment" {
  listener_arn    = aws_lb_listener.muroom_alb_https_listener.arn
  certificate_arn = data.aws_acm_certificate.dev_cert.arn
}

# --------------------------------------------------------
# 4. Listener Rules (Host Header 기반 라우팅)
# --------------------------------------------------------
# [운영] api.muroom.kr 요청(Host 헤더(도메인 이름)가 'api.muroom.kr'인 요청)은 Prod Target Group으로
resource "aws_lb_listener_rule" "prod_host_rule" {
  listener_arn = aws_lb_listener.muroom_alb_https_listener.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.muroom_alb_prod_tg.arn
  }

  condition {
    host_header {
      values = ["api.muroom.kr"]
    }
  }
}

# [개발] dev-api.muroom.kr 요청(Host 헤더(도메인 이름)가 'dev-api.muroom.kr'인 요청)은 Dev Target Group으로
resource "aws_lb_listener_rule" "dev_host_rule" {
  listener_arn = aws_lb_listener.muroom_alb_https_listener.arn
  priority     = 20

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
