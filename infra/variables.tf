variable "aws_region" {
  description = "AWS 리전 설정"
  type        = string
  default     = "ap-northeast-2"
}

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

variable "db_prod_dbname" {
  description = "데이터베이스 이름"
  type        = string
  sensitive   = true
}
variable "db_prod_username" {
  description = "데이터베이스 관리자 사용자 이름"
  type        = string
  sensitive   = true
}

variable "db_dev_dbname" {
  description = "데이터베이스 이름"
  type        = string
  sensitive   = true
}
variable "db_dev_username" {
  description = "데이터베이스 관리자 사용자 이름"
  type        = string
  sensitive   = true
}

variable "db_ebs_mount_point" {
  description = "데이터베이스 EBS 볼륨 마운트 포인트"
  type        = string
  sensitive   = true
}

variable "valkey_prod_username" {
  description = "Valkey 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "valkey_prod_password" {
  description = "Valkey 관리자 비밀번호"
  type        = string
  sensitive   = true
}
variable "valkey_dev_username" {
  description = "Valkey 관리자 사용자 이름"
  type        = string
  sensitive   = true
}
variable "valkey_dev_password" {
  description = "Valkey 관리자 비밀번호"
  type        = string
  sensitive   = true
}
variable "valkey_ebs_mount_point" {
  description = "Valkey EBS 볼륨 마운트 포인트"
  type        = string
  sensitive   = true
}
