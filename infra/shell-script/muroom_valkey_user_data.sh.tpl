#!/bin/bash
set -e
# user-data 스크립트의 모든 출력을 로그 파일과 콘솔로 보냅니다.
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "INFO: Valkey on Docker setup script starting..."

# --- 1. Terraform으로부터 변수 받기 ---
EBS_VOLUME_ID="${ebs_volume_id}"
MOUNT_POINT="${mount_point}"
VALKEY_VERSION="${valkey_version}"
VALKEY_SECRET_ARN="${valkey_secret_arn}"
AWS_REGION="${aws_region}"

echo "INFO: Target EBS Volume ID: $EBS_VOLUME_ID"
echo "INFO: Mount Point: $MOUNT_POINT"

# --- 2. EBS 볼륨 장치 경로 동적 찾기 및 마운트 (DB 스크립트와 동일한 안정적인 로직) ---
VOLUME_SERIAL=$(echo $EBS_VOLUME_ID | sed 's/vol-//')
DEVICE_PATH=""

for i in $(seq 1 20); do
  for dev in $(ls /dev/nvme*n1 2>/dev/null); do
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
if ! file -s $DEVICE_PATH | grep -q "filesystem"; then
  echo "INFO: No filesystem found on $DEVICE_PATH. Formatting as xfs."
  mkfs -t xfs $DEVICE_PATH
else
  echo "INFO: Filesystem already exists on $DEVICE_PATH. Skipping format."
fi
mkdir -p $MOUNT_POINT
mount $DEVICE_PATH $MOUNT_POINT
UUID=$(blkid -s UUID -o value $DEVICE_PATH)
if ! grep -q "$UUID" /etc/fstab; then
  echo "INFO: Adding mount to /etc/fstab."
  echo "UUID=$UUID  $MOUNT_POINT  xfs  defaults,nofail  0  2" >> /etc/fstab
fi
echo "INFO: EBS volume mounted successfully."


# --- 3. Docker 및 jq 설치 ---
echo "INFO: Installing Docker and jq..."
dnf update -y
dnf install -y docker jq
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user
echo "INFO: Docker and jq installed and started."


# --- 4. Secrets Manager에서 자격 증명 가져오기 및 컨테이너 실행 ---
echo "INFO: Fetching Valkey credentials from Secrets Manager..."
SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id $VALKEY_SECRET_ARN --region $AWS_REGION --query SecretString --output text)
if [ -z "$SECRET_JSON" ]; then
  echo "ERROR: Failed to retrieve Valkey secret from Secrets Manager. Exiting."
  exit 1
fi

VALKEY_USERNAME=$(echo $SECRET_JSON | jq -r .username)
VALKEY_PASSWORD=$(echo $SECRET_JSON | jq -r .password)

if [ -z "$VALKEY_USERNAME" ] || [ -z "$VALKEY_PASSWORD" ]; then
  echo "ERROR: Failed to parse username or password from secret. Exiting."
  exit 1
fi

echo "INFO: Running Valkey container with credentials from Secrets Manager..."
VALKEY_DATA_PATH="$MOUNT_POINT"

# Valkey Docker 이미지는 컨테이너 내부의 valkey 사용자가 데이터 폴더에 접근해야 합니다.
# 일반적으로 valkey/redis의 UID/GID는 999 또는 1000, 1001 등 이미지마다 다를 수 있습니다.
# 여기서는 일반적인 999를 사용합니다.
chown -R 999:999 $VALKEY_DATA_PATH

# Valkey 7.2부터 도입된 ACL(Access Control List)을 사용하여 사용자/비밀번호를 설정합니다.
docker run -d \
  --name valkey \
  -p 6379:6379 \
  -v $VALKEY_DATA_PATH:/data \
  --restart always \
  valkey/valkey:${valkey_version} \
  --user $VALKEY_USERNAME on ">$VALKEY_PASSWORD" ~* +@all
  --appendonly yes

echo "INFO: Valkey container started successfully!"