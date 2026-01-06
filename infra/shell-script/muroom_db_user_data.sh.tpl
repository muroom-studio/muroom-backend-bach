#!/bin/bash
set -e
# user-data 스크립트의 모든 출력을 로그 파일과 콘솔로 보냅니다.
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "INFO: PostgreSQL on Docker setup script starting..."

# --- 1. Terraform으로부터 변수 받기 ---
EBS_VOLUME_ID="${ebs_volume_id}"
MOUNT_POINT="${mount_point}"
PG_VERSION="${pg_version}"
DB_SECRET_ARN="${db_secret_arn}"
AWS_REGION="${aws_region}"

echo "INFO: Target EBS Volume ID: $EBS_VOLUME_ID"
echo "INFO: Mount Point: $MOUNT_POINT"

# --- 2. EBS 볼륨 장치 경로 동적 찾기 및 마운트 ---
# Nitro 시스템에서 NVMe EBS 볼륨은 'nvme'로 시작하는 장치 이름을 가집니다.
# 볼륨 ID에서 "vol-" 접두사를 제거하여 시리얼 번호를 만듦
VOLUME_SERIAL=$(echo $EBS_VOLUME_ID | sed 's/vol-//')
DEVICE_PATH=""

# NVMe 장치 경로를 먼저 탐색
for i in $(seq 1 20); do
  # nvme id -v <장치경로> 를 통해 볼륨 ID를 확인
  for dev in $(ls /dev/nvme*n1); do
    vol_id=$(nvme id-ctrl -v $dev | grep -o 'vol-[0-9a-f]*' || true)
    if [ "$vol_id" == "$EBS_VOLUME_ID" ]; then
      DEVICE_PATH=$dev
      echo "INFO: Found NVMe device $DEVICE_PATH for Volume ID $EBS_VOLUME_ID"
      break 2
    fi
  done
  echo "Waiting for NVMe device for volume $EBS_VOLUME_ID to appear... (Attempt $i/20)"
  sleep 3
done

# NVMe를 찾지 못하면 기존 방식으로 재시도
if [ -z "$DEVICE_PATH" ]; then
  echo "INFO: Could not find NVMe device, trying older device naming scheme..."
  for i in $(seq 1 20); do
    FOUND_DEVICE=$(lsblk -no NAME,SERIAL | grep -E "^[hsv]d[a-z]" | grep "$VOLUME_SERIAL" | awk '{print $1}')
    if [ -n "$FOUND_DEVICE" ]; then
      DEVICE_PATH="/dev/$FOUND_DEVICE"
      echo "INFO: Found device $DEVICE_PATH for Volume ID $EBS_VOLUME_ID"
      break
    fi
    echo "Waiting for device with serial $VOLUME_SERIAL to appear... (Attempt $i/20)"
    sleep 3
  done
fi

if [ -z "$DEVICE_PATH" ]; then
  echo "ERROR: Could not find device for Volume ID $EBS_VOLUME_ID after 120 seconds. Exiting."
  exit 1
fi

echo "INFO: Mounting EBS volume $DEVICE_PATH..."
# 파일시스템이 없으면 xfs로 포맷
if ! file -s $DEVICE_PATH | grep -q "filesystem"; then
  echo "INFO: No filesystem found on $DEVICE_PATH. Formatting as xfs."
  mkfs -t xfs $DEVICE_PATH
else
  echo "INFO: Filesystem already exists on $DEVICE_PATH. Skipping format."
fi
# 마운트 및 /etc/fstab에 등록
mkdir -p $MOUNT_POINT
mount $DEVICE_PATH $MOUNT_POINT
UUID=$(blkid -s UUID -o value $DEVICE_PATH)
if ! grep -q "$UUID" /etc/fstab; then
  echo "INFO: Adding mount to /etc/fstab."
  echo "UUID=$UUID  $MOUNT_POINT  xfs  defaults,nofail  0  2" >> /etc/fstab
fi
echo "INFO: EBS volume mounted successfully."


# --- 3. Docker 설치 및 시작 ---
echo "INFO: Installing Docker and jq..."
dnf update -y
dnf install -y docker jq
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user
echo "INFO: Docker and jq installed and started."


# --- 4. Secrets Manager에서 자격 증명(credential) 가져오기 및 컨테이너 실행 ---
echo "INFO: Fetching DB password from Secrets Manager..."
SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id $DB_SECRET_ARN --region $AWS_REGION --query SecretString --output text)
if [ -z "$SECRET_JSON" ]; then
  echo "ERROR: Failed to retrieve DB password from Secrets Manager. Exiting."
  exit 1
fi

DB_NAME=$(echo $SECRET_JSON | jq -r .dbname)
DB_USERNAME=$(echo $SECRET_JSON | jq -r .username)
DB_PASSWORD=$(echo $SECRET_JSON | jq -r .password)

if [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ] || [ -z "$DB_NAME" ]; then
  echo "ERROR: Failed to parse dbname, username, or password from secret. Exiting."
  exit 1
fi

echo "INFO: Running PostgreSQL container with credentials from Secrets Manager..."
DB_DATA_PATH="$MOUNT_POINT"
chown -R 999:999 $DB_DATA_PATH # postgres 컨테이너의 기본 사용자 ID/그룹 ID

docker run -d --name postgres \
  -p 5432:5432 \
  -v $DB_DATA_PATH:/var/lib/postgresql/data \
  -e POSTGRES_DB=$DB_NAME \
  -e POSTGRES_USER=$DB_USERNAME \
  -e POSTGRES_PASSWORD=$DB_PASSWORD \
  --restart always \
  postgres:${pg_version}-alpine

echo "INFO: PostgreSQL container started successfully!"