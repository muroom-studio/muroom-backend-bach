#!/bin/bash
# 3단계: Terraform 밖 잔여 리소스 정리 (2026-09-02)
# 사전 조건: DB 덤프 + S3 전체 아카이브 완료, terraform destroy 완료
# 실행: AWS_PROFILE=monte-muroom bash cleanup-step3.sh
set -u
export AWS_PROFILE=${AWS_PROFILE:-monte-muroom}
export AWS_PAGER=""

echo "=========================================="
echo "[1/8] temp 인스턴스 종료"
echo "=========================================="
aws ec2 terminate-instances --instance-ids i-0b89e9c1fd86a31d0 \
  --query 'TerminatingInstances[0].CurrentState.Name' --output text || true

echo "=========================================="
echo "[2/8] 고아 RDS muroom-postgres 삭제 (자동 스냅샷 8개 포함)"
echo "=========================================="
aws rds delete-db-instance --db-instance-identifier muroom-postgres \
  --skip-final-snapshot --delete-automated-backups \
  --query 'DBInstance.DBInstanceStatus' --output text || true

echo "=========================================="
echo "[3/8] 구 시크릿 3종 즉시 삭제 (유예기간 없음)"
echo "=========================================="
for s in muroom-bach-jwt-secret muroom-bach-database-secret muroom-bach-storage-secret; do
  aws secretsmanager delete-secret --secret-id "$s" --force-delete-without-recovery \
    --query 'Name' --output text || true
done
# rds!db-* 시크릿은 RDS 삭제와 함께 자동 정리됨

echo "=========================================="
echo "[4/8] 구 로그 그룹 2개 삭제"
echo "=========================================="
aws logs delete-log-group --log-group-name /ecs/muroom-bach-task-definition || true
aws logs delete-log-group --log-group-name /elastic-cache/muroom-bach-prod-cache || true

echo "=========================================="
echo "[5/8] ACM 인증서 2개 삭제 (api / dev-api.muroom.kr)"
echo "=========================================="
for arn in $(aws acm list-certificates --query 'CertificateSummaryList[].CertificateArn' --output text); do
  echo "삭제: $arn"
  aws acm delete-certificate --certificate-arn "$arn" || true
done

echo "=========================================="
echo "[6/8] DLM EBS 스냅샷 전체 삭제 (36개)"
echo "=========================================="
for snap in $(aws ec2 describe-snapshots --owner-ids self --query 'Snapshots[].SnapshotId' --output text); do
  aws ec2 delete-snapshot --snapshot-id "$snap" && echo "삭제됨: $snap" || true
done

echo "=========================================="
echo "[7/8] RDS 최종 아카이브 스냅샷 삭제"
echo "  (beta 데이터 불필요 확정 — 로컬 덤프/아카이브로 대체됨)"
echo "=========================================="
aws rds delete-db-snapshot --db-snapshot-identifier muroom-postgres-final-archive-20260803 \
  --query 'DBSnapshot.Status' --output text || true

echo "=========================================="
echo "[8/8] 잔여 S3 버킷 6개 완전 삭제 (버전 포함)"
echo "  ※ muroom-terraform-state-backend는 4단계에서 별도 처리"
echo "=========================================="
empty_and_delete_bucket() {
  local b=$1
  echo "--- $b 비우는 중..."
  aws s3 rm "s3://$b" --recursive --only-show-errors || true
  # 버전 객체 + delete marker 반복 제거
  while :; do
    payload=$(aws s3api list-object-versions --bucket "$b" --max-keys 500 --output json 2>/dev/null \
      | jq '{Objects: (((.Versions // []) + (.DeleteMarkers // [])) | map({Key, VersionId})), Quiet: true}')
    cnt=$(echo "$payload" | jq '.Objects | length')
    [ "$cnt" -eq 0 ] && break
    echo "$payload" > /tmp/_delvers.json
    aws s3api delete-objects --bucket "$b" --delete file:///tmp/_delvers.json > /dev/null || break
    echo "  버전 $cnt개 삭제"
  done
  aws s3api delete-bucket --bucket "$b" && echo "--- $b 삭제 완료" || echo "--- $b 삭제 실패"
}
for b in mr-dev-private-storage mr-dev-public-storage mr-prod-private-storage mr-prod-public-storage muroom-storage muroom-bach-dev-storage; do
  empty_and_delete_bucket "$b"
done

echo "=========================================="
echo "[마무리] temp 인스턴스·RDS 종료 대기 후 구 VPC 해체"
echo "=========================================="
echo "temp 인스턴스 종료 대기 중..."
aws ec2 wait instance-terminated --instance-ids i-0b89e9c1fd86a31d0 || true
echo "RDS 삭제 대기 중 (수 분 소요)..."
aws rds wait db-instance-deleted --db-instance-identifier muroom-postgres || true

V=vpc-0a51db2d329d820a2
echo "--- IGW 분리/삭제"
aws ec2 detach-internet-gateway --internet-gateway-id igw-0ede01d363f54bfc1 --vpc-id $V || true
aws ec2 delete-internet-gateway --internet-gateway-id igw-0ede01d363f54bfc1 || true
echo "--- RDS 서브넷 그룹 삭제 (있으면)"
for sng in $(aws rds describe-db-subnet-groups --query 'DBSubnetGroups[].DBSubnetGroupName' --output text); do
  aws rds delete-db-subnet-group --db-subnet-group-name "$sng" && echo "삭제됨: $sng" || true
done
echo "--- 서브넷 삭제"
aws ec2 delete-subnet --subnet-id subnet-06cf164ca7860bffa || true
aws ec2 delete-subnet --subnet-id subnet-015e5c2fc85de8f3d || true
echo "--- 라우팅 테이블 삭제 (main 제외)"
aws ec2 delete-route-table --route-table-id rtb-0244df7f4037fb88b || true
aws ec2 delete-route-table --route-table-id rtb-0e0883e6338be5396 || true
echo "--- 보안그룹 삭제 (default 제외)"
aws ec2 delete-security-group --group-id sg-0af5d5e0a2c75ec93 || true
aws ec2 delete-security-group --group-id sg-0cfd9e7c87a144b84 || true
echo "--- VPC 삭제"
aws ec2 delete-vpc --vpc-id $V && echo "구 VPC 삭제 완료" || echo "VPC 삭제 실패 — 잔여 의존성 확인 필요"

echo ""
echo "=========================================="
echo "3단계 완료. 남은 것: muroom-terraform-state-backend + DynamoDB 락 테이블 (4단계)"
echo "=========================================="
