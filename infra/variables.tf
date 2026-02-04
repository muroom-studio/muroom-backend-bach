# --------------------------------------------------------
# AWS 설정 변수
# --------------------------------------------------------
variable "aws_region" {
  description = "AWS 리전 설정"
  type        = string
  default     = "ap-northeast-2"
}

# --------------------------------------------------------
# 네트워크 설정 변수
# --------------------------------------------------------
variable "vpc_cidr" {
  description = "VPC의 CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}
variable "public_subnet_2a_cidr" {
  description = "Public Subnet 2a의 CIDR 블록"
  type        = string
  default     = "10.0.11.0/24"
}
variable "public_subnet_2b_cidr" {
  description = "Public Subnet 2b의 CIDR 블록"
  type        = string
  default     = "10.0.12.0/24"
}
variable "private_subnet_2a_cidr" {
  description = "Private Subnet 2a의 CIDR 블록"
  type        = string
  default     = "10.0.21.0/24"
}
variable "private_subnet_2b_cidr" {
  description = "Private Subnet 2b의 CIDR 블록"
  type        = string
  default     = "10.0.22.0/24"
}

# --------------------------------------------------------
# Postgres 설정 변수
# --------------------------------------------------------
variable "postgres_prod_dbname" {
  description = "postgres 이름"
  type        = string
  sensitive   = true
}
variable "postgres_prod_port" {
  description = "postgres 포트 번호"
  type        = number
  sensitive   = true
}
variable "postgres_prod_username" {
  description = "postgres 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "postgres_dev_dbname" {
  description = "postgres 이름"
  type        = string
  sensitive   = true
}
variable "postgres_dev_port" {
  description = "postgres 포트 번호"
  type        = number
  sensitive   = true
}
variable "postgres_dev_username" {
  description = "postgres 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "postgres_ebs_mount_point" {
  description = "postgres EBS 볼륨 마운트 포인트"
  type        = string
  sensitive   = true
}

# --------------------------------------------------------
# Valkey 설정 변수
# --------------------------------------------------------
variable "valkey_prod_port" {
  description = "valkey 포트 번호"
  type        = number
  sensitive   = true
}
variable "valkey_prod_username" {
  description = "valkey 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "valkey_dev_port" {
  description = "valkey 포트 번호"
  type        = number
  sensitive   = true
}
variable "valkey_dev_username" {
  description = "valkey 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "valkey_ebs_mount_point" {
  description = "valkey EBS 볼륨 마운트 포인트"
  type        = string
  sensitive   = true
}

# --------------------------------------------------------
# 계정 설정 변수
# --------------------------------------------------------
variable "alert_email_address" {
  description = "알림 이메일 주소"
  type        = string
  sensitive   = true
}

# --------------------------------------------------------
# 애플리케이션 환경 변수
# --------------------------------------------------------
# 운영용
variable "seoul_api_key_prod" {
  description = "서울시 열린데이터광장 API 키"
  type        = string
  sensitive   = true
}
variable "juso_search_api_key_prod" {
  description = "주소 검색 API 키"
  type        = string
  sensitive   = true
}
variable "juso_coord_api_key_prod" {
  description = "주소 좌표 변환 API 키"
  type        = string
  sensitive   = true
}
variable "kakao_rest_api_key_prod" {
  description = "카카오 REST API 키"
  type        = string
  sensitive   = true
}
variable "kakao_client_id_prod" {
  description = "카카오 클라이언트 ID"
  type        = string
  sensitive   = true
}
variable "kakao_client_secret_prod" {
  description = "카카오 클라이언트 시크릿"
  type        = string
  sensitive   = true
}
variable "google_client_id_prod" {
  description = "구글 클라이언트 ID"
  type        = string
  sensitive   = true
}
variable "google_client_secret_prod" {
  description = "구글 클라이언트 시크릿"
  type        = string
  sensitive   = true
}
variable "naver_access_key_id_prod" {
  description = "네이버 액세스 키 ID"
  type        = string
  sensitive   = true
}
variable "naver_secret_key_prod" {
  description = "네이버 시크릿 키"
  type        = string
  sensitive   = true
}
variable "naver_sens_service_id_prod" {
  description = "네이버 SENS 서비스 ID"
  type        = string
  sensitive   = true
}
variable "naver_sens_from_prod" {
  description = "네이버 SENS 발신 번호"
  type        = string
  sensitive   = true
}

# 개발용
variable "seoul_api_key_dev" {
  description = "서울시 열린데이터광장 API 키"
  type        = string
  sensitive   = true
}
variable "juso_search_api_key_dev" {
  description = "주소 검색 API 키"
  type        = string
  sensitive   = true
}
variable "juso_coord_api_key_dev" {
  description = "주소 좌표 변환 API 키"
  type        = string
  sensitive   = true
}
variable "kakao_rest_api_key_dev" {
  description = "카카오 REST API 키"
  type        = string
  sensitive   = true
}
variable "kakao_client_id_dev" {
  description = "카카오 클라이언트 ID"
  type        = string
  sensitive   = true
}
variable "kakao_client_secret_dev" {
  description = "카카오 클라이언트 시크릿"
  type        = string
  sensitive   = true
}
variable "google_client_id_dev" {
  description = "구글 클라이언트 ID"
  type        = string
  sensitive   = true
}
variable "google_client_secret_dev" {
  description = "구글 클라이언트 시크릿"
  type        = string
  sensitive   = true
}
variable "naver_access_key_id_dev" {
  description = "네이버 액세스 키 ID"
  type        = string
  sensitive   = true
}
variable "naver_secret_key_dev" {
  description = "네이버 시크릿 키"
  type        = string
  sensitive   = true
}
variable "naver_sens_service_id_dev" {
  description = "네이버 SENS 서비스 ID"
  type        = string
  sensitive   = true
}
variable "naver_sens_from_dev" {
  description = "네이버 SENS 발신 번호"
  type        = string
  sensitive   = true
}
