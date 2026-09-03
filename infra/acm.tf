# 운영용 인증서 조회
data "aws_acm_certificate" "prod_cert" {
  domain   = "api.muroom.kr"
  statuses = ["ISSUED"]
}

# 개발용 인증서 조회
data "aws_acm_certificate" "dev_cert" {
  domain   = "dev-api.muroom.kr"
  statuses = ["ISSUED"]
}
