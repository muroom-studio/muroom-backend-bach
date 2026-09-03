#!/bin/bash
# 3단계 후속: RDS 삭제 보호 해제 → 삭제 → 구 VPC 잔여 해체
set -u
export AWS_PROFILE=${AWS_PROFILE:-monte-muroom}
export AWS_PAGER=""

echo "[1] RDS 삭제 보호 해제"
aws rds modify-db-instance --db-instance-identifier muroom-postgres \
  --no-deletion-protection --apply-immediately \
  --query 'DBInstance.DeletionProtection' --output text

echo "[2] RDS 삭제"
aws rds delete-db-instance --db-instance-identifier muroom-postgres \
  --skip-final-snapshot --delete-automated-backups \
  --query 'DBInstance.DBInstanceStatus' --output text

echo "[3] RDS 삭제 완료 대기 (수 분 소요)..."
aws rds wait db-instance-deleted --db-instance-identifier muroom-postgres || \
aws rds wait db-instance-deleted --db-instance-identifier muroom-postgres

echo "[4] RDS 서브넷 그룹 삭제"
aws rds delete-db-subnet-group --db-subnet-group-name muroom-database-subnet-group || true

echo "[5] 구 VPC 잔여 해체"
aws ec2 delete-subnet --subnet-id subnet-015e5c2fc85de8f3d || true
aws ec2 delete-route-table --route-table-id rtb-0244df7f4037fb88b || true
aws ec2 delete-security-group --group-id sg-0af5d5e0a2c75ec93 || true
aws ec2 delete-vpc --vpc-id vpc-0a51db2d329d820a2 && echo "구 VPC 삭제 완료" || echo "VPC 삭제 실패"
