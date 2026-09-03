# --------------------------------------------------------
# Terraform 및 Backend 설정
# --------------------------------------------------------
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }

  backend "s3" {
    bucket         = "muroom-terraform-state-backend"
    key            = "muroom/infra/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "muroom-terraform-state-lock"
    encrypt        = true
  }
}

# --------------------------------------------------------
# AWS Provider 설정
# --------------------------------------------------------
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      ManagedBy = "Terraform"
      Project   = "Muroom"
    }
  }
}

data "aws_region" "current" {}

# --------------------------------------------------------
# AMI 데이터 소스 설정
# --------------------------------------------------------
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-minimal-2023.*-kernel-6.12-arm64"]
  }

  filter {
    name   = "architecture"
    values = ["arm64"]
  }
}
data "aws_ami" "ecs_optimized_al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-ecs-hvm-*-arm64"]
  }

  filter {
    name   = "architecture"
    values = ["arm64"]
  }
}
