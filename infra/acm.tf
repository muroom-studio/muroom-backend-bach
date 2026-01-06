# Route 53에 등록된 muroom.kr 도메인의 Hosted Zone 정보를 가져옵니다.
data "aws_route53_zone" "primary" {
  name         = "muroom.kr."
  private_zone = false
}

# 1. api.muroom.kr와 *.api.muroom.kr을 모두 포함하는 단일 ACM 인증서 생성
resource "aws_acm_certificate" "alb_cert" {
  domain_name               = "api.muroom.kr"
  subject_alternative_names = ["dev-api.muroom.kr"]
  validation_method         = "DNS"
  lifecycle {
    create_before_destroy = true # 인증서 교체 시 다운타임 방지
  }
  tags = {
    Name = "muroom-api-certificate"
  }
}

# 2. 위 인증서의 두 도메인 모두에 대한 DNS 검증 레코드 자동 생성
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.alb_cert.domain_validation_options : dvo.domain_name => dvo
  }
  allow_overwrite = true
  name            = each.value.resource_record_name
  records         = [each.value.resource_record_value]
  ttl             = 60
  type            = each.value.resource_record_type
  zone_id         = data.aws_route53_zone.primary.zone_id
}

# 3. ACM 인증서가 완전히 발급될 때까지 Terraform 실행 대기
resource "aws_acm_certificate_validation" "cert_validation" {
  certificate_arn         = aws_acm_certificate.alb_cert.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}
