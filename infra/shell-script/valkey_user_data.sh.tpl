#!/bin/bash
set -e

# 모든 출력을 로그 파일(/var/log/user-data.log)로 저장합니다.
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "INFO: Valkey on Docker Setup Script Starting..."

# --- 1. 필수 패키지 설치 ---
echo "INFO: Installing Docker, JQ, NVMe-CLI, and SSM Agent..."
dnf update -y
dnf install -y docker jq nvme-cli cronie https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_arm64/amazon-ssm-agent.rpm

# --- 2. 서비스 시작 및 권한 설정 ---
# Docker 로그가 디스크를 다 먹지 않도록 제한 설정 (최대 10MB 파일 3개 = 30MB)
mkdir -p /etc/docker
cat <<EOF > /etc/docker/daemon.json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

systemctl start docker
systemctl enable docker
systemctl start amazon-ssm-agent
systemctl enable amazon-ssm-agent
systemctl start crond
systemctl enable crond

usermod -a -G docker ec2-user
id -u ssm-user &>/dev/null || useradd -m ssm-user
usermod -a -G docker ssm-user

# ssm-user에게 비밀번호 없이 sudo 권한 부여
echo "ssm-user ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/ssm-user
chmod 440 /etc/sudoers.d/ssm-user

# --- 3. Terraform 변수 주입 (일관성 유지) ---
EBS_VOLUME_ID="${ebs_volume_id}"
MOUNT_POINT="${mount_point}"
VALKEY_SECRET_ARN="${valkey_secret_arn}"
AWS_REGION="${aws_region}"
BACKUP_S3_BUCKET_NAME="${backup_s3_bucket_name}"

# --- 4. EBS 볼륨 마운트 로직 (PostgreSQL 스크립트와 동일하게 통일) ---
echo "INFO: Locating EBS volume $EBS_VOLUME_ID..."
SERIAL_ID=`echo $EBS_VOLUME_ID | sed 's/vol-//'`

DEVICE_PATH=`lsblk -dpno NAME,SERIAL | grep "$SERIAL_ID" | awk '{print $1}'`

if [ -z "$DEVICE_PATH" ]; then
    for i in `seq 1 12`; do
        echo "Waiting for device $EBS_VOLUME_ID to appear... ($i/12)"
        sleep 5
        DEVICE_PATH=`lsblk -dpno NAME,SERIAL | grep "$SERIAL_ID" | awk '{print $1}'`
        [ -n "$DEVICE_PATH" ] && break
    done
fi

if [ -z "$DEVICE_PATH" ]; then
    echo "ERROR: Could not find device for $EBS_VOLUME_ID after 60s."
    exit 1
fi

echo "INFO: Found device $DEVICE_PATH. Checking filesystem..."
if ! blkid $DEVICE_PATH | grep -q 'TYPE="xfs"'; then
    echo "INFO: Formatting $DEVICE_PATH as xfs..."
    mkfs -t xfs $DEVICE_PATH
fi

mkdir -p $MOUNT_POINT
mountpoint -q $MOUNT_POINT || mount $DEVICE_PATH $MOUNT_POINT

UUID=`blkid -s UUID -o value $DEVICE_PATH`
grep -q "$UUID" /etc/fstab || echo "UUID=$UUID $MOUNT_POINT xfs defaults,nofail 0 2" >> /etc/fstab

# --- 5. OS 최적화 설정 (Valkey 전용) ---
echo "INFO: Optimizing OS for Valkey..."
cat <<EOF >> /etc/sysctl.conf
vm.overcommit_memory=1
net.core.somaxconn=1024
net.ipv4.tcp_max_syn_backlog=1024
EOF
sysctl -p

# THP 비활성화 서비스 등록 (재부팅 대비)
cat <<EOF > /etc/systemd/system/disable-thp.service
[Unit]
Description=Disable Transparent Huge Pages (THP)
After=sysinit.target local-fs.target
Before=docker.service

[Service]
Type=oneshot
ExecStart=/bin/sh -c 'echo never > /sys/kernel/mm/transparent_hugepage/enabled && echo never > /sys/kernel/mm/transparent_hugepage/defrag'

[Install]
WantedBy=basic.target
EOF

systemctl daemon-reload
systemctl enable --now disable-thp.service

# --- 6. Secrets Manager에서 자격 증명 가져오기 ---
echo "INFO: Fetching Valkey credentials from Secrets Manager..."
SECRET_JSON=`aws secretsmanager get-secret-value --secret-id $VALKEY_SECRET_ARN --region $AWS_REGION --query SecretString --output text`

VALKEY_USERNAME=`echo $SECRET_JSON | jq -r .username`
VALKEY_PASSWORD=`echo $SECRET_JSON | jq -r .password`

if [ -z "$VALKEY_USERNAME" ] || [ -z "$VALKEY_PASSWORD" ]; then
    echo "ERROR: Failed to parse username or password. Exiting."
    exit 1
fi

# --- 7. Valkey 컨테이너 실행 ---
echo "INFO: Starting Valkey container..."
# Postgres와 일관성을 위해 /data 하위 구조 권장하나, 사용자님의 $MOUNT_POINT 유지
mkdir -p $MOUNT_POINT
chown -R 999:999 $MOUNT_POINT

docker run -d --name muroom-valkey \
  -p 6379:6379 \
  -v $MOUNT_POINT:/data \
  --restart always \
  valkey/valkey:8.1.5 \
  valkey-server \
  --user default off \
  --user "$VALKEY_USERNAME" on ">$VALKEY_PASSWORD" ~* +@all \
  --appendonly yes \
  --auto-aof-rewrite-percentage 100 \
  --auto-aof-rewrite-min-size 64mb \
  --save 60 1 \
  --dir /data \
  --dbfilename "muroom_valkey_backup.rdb" \
  --loglevel notice

echo "INFO: Valkey container started successfully!"

# --- 8. Valkey S3 백업 스크립트 생성 ---
# 8-1. 백업 스크립트 작성
echo "INFO: Creating Valkey backup script, service, and timer..."

cat <<EOF > /home/ec2-user/valkey_backup_to_s3.sh
#!/bin/bash
TIMESTAMP=\$(date +%Y%m%d_%H%M%S)
SOURCE_FILE="$MOUNT_POINT/muroom_valkey_backup.rdb"
S3_PATH="s3://$BACKUP_S3_BUCKET_NAME/backups/valkey/\$TIMESTAMP/muroom_valkey_backup.rdb"

echo "INFO: Initiating Valkey BGSAVE for backup..."
# ACL 환경에서는 유저명과 패스워드를 모두 명시해야 SAVE 가능
docker exec -e REDISCLI_AUTH="$VALKEY_PASSWORD" muroom-valkey valkey-cli --user '$VALKEY_USERNAME' BGSAVE

TIMEOUT=120
COUNTER=0

echo "INFO: Waiting for BGSAVE to complete..."
while [ "\$(docker exec -e REDISCLI_AUTH="$VALKEY_PASSWORD" muroom-valkey valkey-cli --user '$VALKEY_USERNAME' info persistence | grep rdb_bgsave_in_progress | cut -d: -f2 | tr -d '\r')" == "1" ]; do
    if [ "\$COUNTER" -gt "\$TIMEOUT" ]; then
        echo "ERROR: BGSAVE timed out after 120 seconds."
        exit 1
    fi
    sleep 2
    let COUNTER=COUNTER+1
done

echo "INFO: BGSAVE completed."

# Uploading backup to S3
if aws s3 cp "\$SOURCE_FILE" "\$S3_PATH"; then
    echo "SUCCESS: Valkey backup uploaded to S3."
else
    echo "ERROR: Valkey backup failed."
    exit 1
fi
EOF

chmod +x /home/ec2-user/valkey_backup_to_s3.sh
chown ec2-user:ec2-user /home/ec2-user/valkey_backup_to_s3.sh

# 8-2. systemd 서비스 등록 (User=root 적용)
cat <<EOF > /etc/systemd/system/valkey-backup.service
[Unit]
Description=Valkey RDB Backup to S3
After=docker.service

[Service]
Type=oneshot
User=root
ExecStart=/bin/bash /home/ec2-user/valkey_backup_to_s3.sh
EOF

# 8-3. systemd 타이머 등록 (매일 새벽 3시 KST 실행)
cat <<EOF > /etc/systemd/system/valkey-backup.timer
[Unit]
Description=Run Valkey Backup daily at 3AM KST (18:00 UTC)

[Timer]
OnCalendar=*-*-* 18:00:00
Persistent=true
Unit=valkey-backup.service

[Install]
WantedBy=timers.target
EOF

# 서비스 반영 및 타이머 활성화
systemctl daemon-reload
systemctl enable --now valkey-backup.timer

echo "INFO: Valkey backup service and timer created successfully!"

echo "INFO: All Setup Completed Successfully!"