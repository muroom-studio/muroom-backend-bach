#!/bin/bash
# 4단계 (최종): Terraform 상태 백엔드 해체 + 삭제 예약 시크릿 즉시 정리
# ※ 이 스크립트 실행 후에는 terraform 상태가 완전히 사라짐 (모든 리소스가 이미 삭제됐으므로 무방)
set -u
export AWS_PROFILE=${AWS_PROFILE:-monte-muroom}
export AWS_PAGER=""

echo "[1] state 버킷 비우기 (버전 포함)"
b=muroom-terraform-state-backend
aws s3 rm "s3://$b" --recursive --only-show-errors || true
while :; do
  payload=$(aws s3api list-object-versions --bucket "$b" --max-keys 500 --output json 2>/dev/null \
    | jq '{Objects: (((.Versions // []) + (.DeleteMarkers // [])) | map({Key, VersionId})), Quiet: true}')
  cnt=$(echo "$payload" | jq '.Objects | length')
  [ "$cnt" -eq 0 ] && break
  echo "$payload" > /tmp/_delvers.json
  aws s3api delete-objects --bucket "$b" --delete file:///tmp/_delvers.json > /dev/null || break
  echo "  버전 ${cnt}개 삭제"
done

echo "[2] state 버킷 삭제"
aws s3api delete-bucket --bucket "$b" && echo "버킷 삭제 완료" || echo "버킷 삭제 실패"

echo "[3] DynamoDB 락 테이블 삭제"
aws dynamodb delete-table --table-name muroom-terraform-state-lock \
  --query 'TableDescription.TableStatus' --output text || true

echo "[4] 삭제 예약된 시크릿 8개 즉시 삭제 (유예기간 단축)"
for s in "muroom/postgres-prod/credentials" "muroom/postgres-dev/credentials" \
         "muroom/valkey-prod/credentials" "muroom/valkey-dev/credentials" \
         "muroom/jwt-secret-key-prod" "muroom/jwt-secret-key-dev" \
         "muroom/api-keys-prod" "muroom/api-keys-dev"; do
  aws secretsmanager delete-secret --secret-id "$s" --force-delete-without-recovery \
    --query 'Name' --output text || true
done

echo ""
echo "== AWS 해체 전체 완료 =="
