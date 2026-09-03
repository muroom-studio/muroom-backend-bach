# 1. 알림을 보낼 SNS 주제 (우체국 역할)
resource "aws_sns_topic" "muroom_infrastructure_alerts_topic" {
  name = "muroom-infrastructure-alerts-topic"
}

# 2. NAT 인스턴스 상태 체크 알람 (인스턴스가 죽었을 때)
resource "aws_cloudwatch_metric_alarm" "muroom_nat_instance_status_check_failed" {
  alarm_name          = "muroom-nat-instance-status-check-failed"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "0"
  alarm_description   = "NAT 인스턴스가 응답하지 않습니다. 즉시 확인이 필요합니다."
  alarm_actions       = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]
  ok_actions          = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]

  dimensions = {
    InstanceId = aws_instance.muroom_nat_instance.id
  }
}

# 3. NAT 인스턴스 CPU 부하 알람 (트래픽이나 처리량이 몰릴 때)
resource "aws_cloudwatch_metric_alarm" "muroom_nat_instance_high_cpu_usage" {
  alarm_name          = "muroom-nat-instance-high-cpu-usage"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "70" # CPU 사용량이 70% 초과 시 알람 발생
  alarm_description   = "NAT 인스턴스 CPU 사용량이 70%를 초과했습니다. 사양 업그레이드를 고려하세요."
  alarm_actions       = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]
  ok_actions          = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]

  dimensions = {
    InstanceId = aws_instance.muroom_nat_instance.id
  }
}

# 4. Prod Postgres 인스턴스 상태 체크 알람 (서버 다운 시)
resource "aws_cloudwatch_metric_alarm" "muroom_postgres_prod_status_check_failed" {
  alarm_name          = "muroom-postgres-prod-status-check-failed"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Maximum"
  threshold           = "0"
  alarm_description   = "Prod Postgres DB 응답 없음 (Status Check Failed)"
  alarm_actions       = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]
  ok_actions          = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]

  dimensions = {
    InstanceId = aws_instance.muroom_postgres_prod_instance.id
  }
}

# 5. Prod Postgres 인스턴스 CPU 부하 알람 (성능 저하 시)
resource "aws_cloudwatch_metric_alarm" "muroom_postgres_prod_high_cpu_usage" {
  alarm_name          = "muroom-postgres-prod-high-cpu-usage"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2" # 5분씩 2번 연속 (총 10분간) 높을 때만 알림 (일시적 스파이크 제외)
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300" # 5분 단위 측정
  statistic           = "Average"
  threshold           = "80" # CPU 80% 초과 시 알람
  alarm_description   = "Prod Postgres DB CPU 사용량 80% 초과. 사양 업그레이드 검토 필요."
  alarm_actions       = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]
  ok_actions          = [aws_sns_topic.muroom_infrastructure_alerts_topic.arn]

  dimensions = {
    InstanceId = aws_instance.muroom_postgres_prod_instance.id
  }
}

# 6. 내 메일로 알림 구독 설정
resource "aws_sns_topic_subscription" "muroom_infrastructure_alerts_email_subscription" {
  topic_arn = aws_sns_topic.muroom_infrastructure_alerts_topic.arn
  protocol  = "email"
  endpoint  = var.alert_email_address # 알림을 받을 이메일 주소
}
