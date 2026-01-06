# --------------------------------------------------------
# 보안 그룹 생성
# --------------------------------------------------------

# 1. Application Load Balancer 보안 그룹
resource "aws_security_group" "muroom_alb_sg" {
  name        = "muroom-alb-sg"
  description = "Application Load Balancer 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-alb-sg" }
}

# 2. NAT 인스턴스 보안 그룹
resource "aws_security_group" "muroom_nat_sg" {
  name        = "muroom-nat-sg"
  description = "NAT 인스턴스 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-nat-sg" }
}

# 3. ECS 인스턴스 보안 그룹
resource "aws_security_group" "muroom_ecs_instance_prod_sg" {
  name        = "muroom-ecs-service-prod-sg"
  description = "ECS 인스턴스 운영 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-ecs-service-prod-sg" }
}
resource "aws_security_group" "muroom_ecs_instance_dev_sg" {
  name        = "muroom-ecs-service-dev-sg"
  description = "ECS 인스턴스 개발 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-ecs-service-dev-sg" }
}

# 4. Database 인스턴스 보안 그룹
resource "aws_security_group" "muroom_db_prod_sg" {
  name        = "muroom-db-prod-sg"
  description = "Database 인스턴스 운영 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-db-prod-sg" }
}
resource "aws_security_group" "muroom_db_dev_sg" {
  name        = "muroom-db-dev-sg"
  description = "Database 인스턴스 개발 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-db-dev-sg" }
}

# 5. Valkey 인스턴스 보안 그룹
resource "aws_security_group" "muroom_valkey_prod_sg" {
  name        = "muroom-valkey-prod-sg"
  description = "Valkey 인스턴스 운영 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-valkey-prod-sg" }
}
resource "aws_security_group" "muroom_valkey_dev_sg" {
  name        = "muroom-valkey-dev-sg"
  description = "Valkey 인스턴스 개발 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id
  tags        = { Name = "muroom-valkey-dev-sg" }
}

# 6. 암호 교체 Lambda 함수 보안 그룹
resource "aws_security_group" "muroom_db_credential_rotation_lambda_sg" {
  name        = "muroom-db-credential-rotation-lambda-sg"
  description = "DB 암호 교체 Lambda 함수 보안 그룹"
  vpc_id      = aws_vpc.muroom_vpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "모든 아웃바운드 트래픽 허용 (AWS API 호출 등)"
  }

  tags = { Name = "muroom-db-credential-rotation-lambda-sg" }
}

# --------------------------------------------------------
# 보안 그룹 규칙 설정
# --------------------------------------------------------

# --- 1. ALB 보안 그룹 규칙 ---
resource "aws_security_group_rule" "alb_ingress_http_from_internet" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_alb_sg.id
  description       = "외부에서 HTTP 허용"
}

resource "aws_security_group_rule" "alb_ingress_https_from_internet" {
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_alb_sg.id
  description       = "외부에서 HTTPS 허용"
}

resource "aws_security_group_rule" "alb_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_alb_sg.id
  description       = "ALB 모든 아웃바운드 트래픽 허용"
}

# --- 2. NAT 보안 그룹 규칙 ---
resource "aws_security_group_rule" "nat_ingress_from_vpc" {
  type              = "ingress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = [aws_vpc.muroom_vpc.cidr_block]
  security_group_id = aws_security_group.muroom_nat_sg.id
  description       = "VPC 내부 트래픽 허용"
}

resource "aws_security_group_rule" "nat_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_nat_sg.id
  description       = "모든 외부 트래픽 허용"
}

# --- 3. ECS 보안 그룹 규칙 ---
resource "aws_security_group_rule" "ecs_prod_ingress_from_alb" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_alb_sg.id
  security_group_id        = aws_security_group.muroom_ecs_instance_prod_sg.id
  description              = "ALB에서 운영 ECS로의 트래픽 허용"
}
resource "aws_security_group_rule" "ecs_dev_ingress_from_alb" {
  type                     = "ingress"
  from_port                = 0
  to_port                  = 0
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_alb_sg.id
  security_group_id        = aws_security_group.muroom_ecs_instance_dev_sg.id
  description              = "ALB에서 개발 ECS로의 트래픽 허용"
}

resource "aws_security_group_rule" "ecs_prod_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_ecs_instance_prod_sg.id
  description       = "운영 ECS 모든 아웃바운드 트래픽 허용"
}
resource "aws_security_group_rule" "ecs_dev_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_ecs_instance_dev_sg.id
  description       = "개발 ECS 모든 아웃바운드 트래픽 허용"
}

# --- 4. Database 보안 그룹 규칙 ---
resource "aws_security_group_rule" "db_prod_ingress_from_ecs_prod" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_ecs_instance_prod_sg.id
  security_group_id        = aws_security_group.muroom_db_prod_sg.id
  description              = "ECS에서 운영 DB로의 PostgreSQL 트래픽 허용"
}
resource "aws_security_group_rule" "db_dev_ingress_from_ecs_dev" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_ecs_instance_dev_sg.id
  security_group_id        = aws_security_group.muroom_db_dev_sg.id
  description              = "ECS에서 개발 DB로의 PostgreSQL 트래픽 허용"
}

resource "aws_security_group_rule" "db_prod_ingress_from_nat" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_nat_sg.id
  security_group_id        = aws_security_group.muroom_db_prod_sg.id
  description              = "NAT에서 운영 DB로의 PostgreSQL 트래픽 허용"
}
resource "aws_security_group_rule" "db_dev_ingress_from_nat" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_nat_sg.id
  security_group_id        = aws_security_group.muroom_db_dev_sg.id
  description              = "NAT에서 개발 DB로의 PostgreSQL 트래픽 허용"
}

resource "aws_security_group_rule" "db_prod_ingress_from_lambda" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_db_credential_rotation_lambda_sg.id
  security_group_id        = aws_security_group.muroom_db_prod_sg.id
  description              = "DB 보안 설정 교체 Lambda에서 운영 DB로의 PostgreSQL 트래픽 허용"
}
resource "aws_security_group_rule" "db_dev_ingress_from_lambda" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_db_credential_rotation_lambda_sg.id
  security_group_id        = aws_security_group.muroom_db_dev_sg.id
  description              = "DB 보안 설정 교체 Lambda에서 개발 DB로의 PostgreSQL 트래픽 허용"
}

resource "aws_security_group_rule" "db_prod_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_db_prod_sg.id
  description       = "운영 DB 모든 아웃바운드 트래픽 허용"
}
resource "aws_security_group_rule" "db_dev_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_db_dev_sg.id
  description       = "개발 DB 모든 아웃바운드 트래픽 허용"
}

# --- 5. Valkey 보안 그룹 규칙 ---
resource "aws_security_group_rule" "valkey_prod_ingress_from_ecs_prod" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_ecs_instance_prod_sg.id
  security_group_id        = aws_security_group.muroom_valkey_prod_sg.id
  description              = "ECS에서 운영 Valkey로의 Valkey 트래픽 허용"
}
resource "aws_security_group_rule" "valkey_dev_ingress_from_ecs_dev" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.muroom_ecs_instance_dev_sg.id
  security_group_id        = aws_security_group.muroom_valkey_dev_sg.id
  description              = "ECS에서 개발 Valkey로의 Valkey 트래픽 허용"
}

resource "aws_security_group_rule" "valkey_prod_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_valkey_prod_sg.id
  description       = "운영 Valkey 모든 아웃바운드 트래픽 허용"
}
resource "aws_security_group_rule" "valkey_dev_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.muroom_valkey_dev_sg.id
  description       = "개발 Valkey 모든 아웃바운드 트래픽 허용"
}
