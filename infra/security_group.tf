# --------------------------------------------------------
# 보안 그룹 생성
# --------------------------------------------------------
# 1. ALB: 외부 트래픽 수신
resource "aws_security_group" "muroom_alb_sg" {
  name        = "muroom-alb-sg"
  description = "Security group for Application Load Balancer"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-alb-sg" }
}

# 2. NAT Instance: Private Subnet 서버들의 아웃바운드 통로
resource "aws_security_group" "muroom_nat_sg" {
  name        = "muroom-nat-sg"
  description = "Security group for NAT Instance"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-nat-sg" }
}

# 3. ECS Instance: 컨테이너 실행 서버(노드)
resource "aws_security_group" "muroom_ecs_instance_prod_sg" {
  name        = "muroom-ecs-service-prod-sg"
  description = "Security group for Prod ECS instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-ecs-service-prod-sg" }
}
resource "aws_security_group" "muroom_ecs_instance_dev_sg" {
  name        = "muroom-ecs-service-dev-sg"
  description = "Security group for Dev ECS instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-ecs-service-dev-sg" }
}

# 4. Postgres Instance: 데이터베이스 서버
resource "aws_security_group" "muroom_postgres_prod_sg" {
  name        = "muroom-postgres-prod-sg"
  description = "Security group for Prod PostgreSQL instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-postgres-prod-sg" }
}
resource "aws_security_group" "muroom_postgres_dev_sg" {
  name        = "muroom-postgres-dev-sg"
  description = "Security group for Dev PostgreSQL instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-postgres-dev-sg" }
}

# 5. Valkey Instance: 캐시 서버
resource "aws_security_group" "muroom_valkey_prod_sg" {
  name        = "muroom-valkey-prod-sg"
  description = "Security group for Prod Valkey instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-valkey-prod-sg" }
}
resource "aws_security_group" "muroom_valkey_dev_sg" {
  name        = "muroom-valkey-dev-sg"
  description = "Security group for Dev Valkey instances"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-valkey-dev-sg" }
}

# 6. Lambda: Postgres 암호 교체용
resource "aws_security_group" "muroom_postgres_credential_rotation_lambda_sg" {
  name        = "muroom-postgres-credential-rotation-lambda-sg"
  description = "Security group for Postgres Credential Rotation Lambda function"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-postgres-credential-rotation-lambda-sg" }
}

# --------------------------------------------------------
# 보안 그룹 규칙 설정
# --------------------------------------------------------
# --- 1. ALB 보안 그룹 규칙 ---
resource "aws_vpc_security_group_ingress_rule" "alb_ingress_http_from_internet" {
  description       = "Allow HTTP traffic from anywhere"
  security_group_id = aws_security_group.muroom_alb_sg.id
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
  cidr_ipv4         = "0.0.0.0/0"
}
resource "aws_vpc_security_group_ingress_rule" "alb_ingress_https_from_internet" {
  description       = "Allow HTTPS traffic from anywhere"
  security_group_id = aws_security_group.muroom_alb_sg.id
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  cidr_ipv4         = "0.0.0.0/0"
}
resource "aws_vpc_security_group_egress_rule" "alb_egress_to_ecs_app_port" {
  for_each = {
    prod = aws_security_group.muroom_ecs_instance_prod_sg.id,
    dev  = aws_security_group.muroom_ecs_instance_dev_sg.id
  }
  description                  = "Allow egress to ECS tasks on port 8080"
  security_group_id            = aws_security_group.muroom_alb_sg.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value
}

# --- 2. NAT Instance 보안 그룹 규칙 ---
resource "aws_vpc_security_group_ingress_rule" "nat_ingress_from_private_subnets" {
  for_each = toset([
    aws_subnet.muroom_private_subnet_2a.cidr_block,
    aws_subnet.muroom_private_subnet_2b.cidr_block
  ])
  description       = "Allow all traffic from private subnets"
  security_group_id = aws_security_group.muroom_nat_sg.id
  ip_protocol       = "-1"
  cidr_ipv4         = each.key
}
resource "aws_vpc_security_group_egress_rule" "nat_egress_to_internet_all" {
  description       = "Allow all traffic to the internet"
  security_group_id = aws_security_group.muroom_nat_sg.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# --- 3. ECS Instance 보안 그룹 규칙 ---
resource "aws_vpc_security_group_ingress_rule" "ecs_ingress_from_alb_app_port" {
  for_each = {
    prod = aws_security_group.muroom_ecs_instance_prod_sg.id,
    dev  = aws_security_group.muroom_ecs_instance_dev_sg.id
  }
  description                  = "Allow ingress from ALB on port 8080"
  security_group_id            = each.value
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
  referenced_security_group_id = aws_security_group.muroom_alb_sg.id
}
resource "aws_vpc_security_group_egress_rule" "ecs_egress_to_postgres" {
  for_each = {
    prod = { sg = aws_security_group.muroom_ecs_instance_prod_sg.id, dest = aws_security_group.muroom_postgres_prod_sg.id },
    dev  = { sg = aws_security_group.muroom_ecs_instance_dev_sg.id, dest = aws_security_group.muroom_postgres_dev_sg.id }
  }
  description                  = "Allow egress to PostgreSQL"
  security_group_id            = each.value.sg
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value.dest
}
resource "aws_vpc_security_group_egress_rule" "ecs_egress_to_valkey" {
  for_each = {
    prod = { sg = aws_security_group.muroom_ecs_instance_prod_sg.id, dest = aws_security_group.muroom_valkey_prod_sg.id },
    dev  = { sg = aws_security_group.muroom_ecs_instance_dev_sg.id, dest = aws_security_group.muroom_valkey_dev_sg.id }
  }
  description                  = "Allow egress to Valkey"
  security_group_id            = each.value.sg
  from_port                    = 6379
  to_port                      = 6379
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value.dest
}
resource "aws_vpc_security_group_egress_rule" "ecs_egress_to_nat" {
  for_each = {
    prod = aws_security_group.muroom_ecs_instance_prod_sg.id,
    dev  = aws_security_group.muroom_ecs_instance_dev_sg.id
  }
  description       = "Allow egress to the internet via NAT"
  security_group_id = each.value
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# --- 4. Postgres Instance 보안 그룹 규칙 ---
resource "aws_vpc_security_group_ingress_rule" "postgres_ingress_from_ecs" {
  for_each = {
    prod = { sg = aws_security_group.muroom_postgres_prod_sg.id, src = aws_security_group.muroom_ecs_instance_prod_sg.id },
    dev  = { sg = aws_security_group.muroom_postgres_dev_sg.id, src = aws_security_group.muroom_ecs_instance_dev_sg.id }
  }
  description                  = "Allow ingress from ECS tasks"
  security_group_id            = each.value.sg
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value.src
}
resource "aws_vpc_security_group_ingress_rule" "postgres_ingress_from_lambda" {
  for_each = {
    prod = aws_security_group.muroom_postgres_prod_sg.id,
    dev  = aws_security_group.muroom_postgres_dev_sg.id
  }
  description                  = "Allow ingress from credential rotation Lambda"
  security_group_id            = each.value
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  referenced_security_group_id = aws_security_group.muroom_postgres_credential_rotation_lambda_sg.id
}
resource "aws_vpc_security_group_egress_rule" "postgres_egress_to_nat" {
  for_each = {
    prod = aws_security_group.muroom_postgres_prod_sg.id,
    dev  = aws_security_group.muroom_postgres_dev_sg.id
  }
  description       = "Allow egress to the internet via NAT for backups"
  security_group_id = each.value
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# --- 5. Valkey Instance 보안 그룹 규칙 ---
resource "aws_vpc_security_group_ingress_rule" "valkey_ingress_from_ecs_cache" {
  for_each = {
    prod = { sg = aws_security_group.muroom_valkey_prod_sg.id, src = aws_security_group.muroom_ecs_instance_prod_sg.id },
    dev  = { sg = aws_security_group.muroom_valkey_dev_sg.id, src = aws_security_group.muroom_ecs_instance_dev_sg.id }
  }
  description                  = "Allow ingress from ECS tasks"
  security_group_id            = each.value.sg
  from_port                    = 6379
  to_port                      = 6379
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value.src
}
resource "aws_vpc_security_group_egress_rule" "valkey_egress_to_nat" {
  for_each = {
    prod = aws_security_group.muroom_valkey_prod_sg.id,
    dev  = aws_security_group.muroom_valkey_dev_sg.id
  }
  description       = "Allow egress to the internet via NAT for backups"
  security_group_id = each.value
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

# --- 6. Lambda 보안 그룹 규칙 ---
resource "aws_vpc_security_group_egress_rule" "lambda_egress_to_postgres_db" {
  for_each = {
    prod = aws_security_group.muroom_postgres_prod_sg.id,
    dev  = aws_security_group.muroom_postgres_dev_sg.id
  }
  description                  = "Allow egress to PostgreSQL for credential rotation"
  security_group_id            = aws_security_group.muroom_postgres_credential_rotation_lambda_sg.id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  referenced_security_group_id = each.value
}
resource "aws_vpc_security_group_egress_rule" "lambda_egress_to_internet_https" {
  description       = "Allow egress to internet for AWS APIs"
  security_group_id = aws_security_group.muroom_postgres_credential_rotation_lambda_sg.id
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  cidr_ipv4         = "0.0.0.0/0"
}
