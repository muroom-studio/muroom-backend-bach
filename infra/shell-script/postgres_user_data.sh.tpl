#!/bin/bash
set -e

# 모든 출력을 로그 파일(/var/log/user-data.log)로 저장합니다.
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "INFO: PostgreSQL + PostGIS(kartoza) Setup Script Starting..."

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
id -u ssm-user &>/dev/null || useradd -m ssm-user # ssm-user가 없으면 생성 (보통 있음)
usermod -a -G docker ssm-user

# --- 3. Terraform 변수 주입 (templatefile에서 넘겨받는 값들) ---
EBS_VOLUME_ID="${ebs_volume_id}"
MOUNT_POINT="${mount_point}"
DB_SECRET_ARN="${postgres_secret_arn}"
AWS_REGION="${aws_region}"
BACKUP_S3_BUCKET_NAME="${backup_s3_bucket_name}"

# --- 4. EBS 볼륨 마운트 로직 ---
echo "INFO: Locating EBS volume $EBS_VOLUME_ID..."
SERIAL_ID=$(echo $EBS_VOLUME_ID | sed 's/vol-//')
DEVICE_PATH=$(lsblk -dpno NAME,SERIAL | grep "$SERIAL_ID" | awk '{print $1}')

if [ -z "$DEVICE_PATH" ]; then
    for i in $(seq 1 12); do
        echo "Waiting for device $EBS_VOLUME_ID to appear... ($i/12)"
        sleep 5
        DEVICE_PATH=$(lsblk -dpno NAME,SERIAL | grep "$SERIAL_ID" | awk '{print $1}')
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

UUID=$(blkid -s UUID -o value $DEVICE_PATH)
grep -q "$UUID" /etc/fstab || echo "UUID=$UUID $MOUNT_POINT xfs defaults,nofail 0 2" >> /etc/fstab

# --- 5. Secrets Manager에서 DB 정보 가져오기 ---
echo "INFO: Fetching secrets from AWS Secrets Manager..."
SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id $DB_SECRET_ARN --region $AWS_REGION --query SecretString --output text)

DB_NAME=$(echo $SECRET_JSON | jq -r .dbname)
DB_USERNAME=$(echo $SECRET_JSON | jq -r .username)
DB_PASSWORD=$(echo $SECRET_JSON | jq -r .password)

# --- 6. WAL 아카이브 경로 준비 ---
echo "INFO: Preparing WAL archive directory..."
DATA_DIR="$MOUNT_POINT/data"
mkdir -p $DATA_DIR
WAL_ARCHIVE_PATH="$MOUNT_POINT/wal_archive"
mkdir -p $WAL_ARCHIVE_PATH

chmod 777 $DATA_DIR
chmod 777 $WAL_ARCHIVE_PATH
# chown -R 999:999 $WAL_ARCHIVE_PATH # kartoza UID와 충돌 방지 위해 주석 처리

# --- 7. TimescaleDB/PostgreSQL 컨테이너 실행 ---
docker run -d --name muroom-postgres \
  -p 5432:5432 \
  -v $DATA_DIR:/var/lib/postgresql \
  -v $WAL_ARCHIVE_PATH:/var/lib/postgresql/wal_archive \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USERNAME" \
  -e POSTGRES_PASS="$DB_PASSWORD" \
  -e ALLOW_IP_RANGE="0.0.0.0/0" \
  --restart always \
  kartoza/postgis:17-3.5 \
  -c "shared_buffers=512MB" \
  -c "max_connections=60" \
  -c "wal_level=replica" \
  -c "archive_mode=on" \
  -c "archive_command=cp %p /var/lib/postgresql/wal_archive/%f"

# --- 8. WAL-to-S3 동기화 스크립트 ---
cat <<EOF > /home/ec2-user/wal_sync_to_s3.sh
#!/bin/bash
WAL_LOCAL_DIR="$WAL_ARCHIVE_PATH"
S3_WAL_PATH="s3://$BACKUP_S3_BUCKET_NAME/backups/postgres/wal"

while true; do
    if [ -d "\$WAL_LOCAL_DIR" ]; then
        if [ -n "\$(ls -A \$WAL_LOCAL_DIR 2>/dev/null)" ]; then
            aws s3 mv \$WAL_LOCAL_DIR/ \$S3_WAL_PATH/ --recursive
            echo "INFO: WAL files synced to S3 at \$(date)"
        fi
    fi
    sleep 10
done
EOF

chmod +x /home/ec2-user/wal_sync_to_s3.sh
chown ec2-user:ec2-user /home/ec2-user/wal_sync_to_s3.sh

cat <<EOF > /etc/systemd/system/wal-sync.service
[Unit]
Description=PostgreSQL WAL Sync to S3
After=docker.service

[Service]
ExecStart=/bin/bash /home/ec2-user/wal_sync_to_s3.sh
Restart=always
User=root
Group=root

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now wal-sync.service

echo "INFO: PITR and WAL Archiving setup completed!"

# --- 9. 기존 pg_dump 기반 백업 스크립트 ---
echo "INFO: Creating backup script..."

cat <<EOF > /home/ec2-user/db_backup_to_s3.sh
#!/bin/bash
TIMESTAMP=\$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/tmp/db_backups"
BACKUP_FILE="muroom_dump_\$TIMESTAMP.sql.gz"

mkdir -p \$BACKUP_DIR

if docker exec -e PGPASSWORD='$DB_PASSWORD' muroom-postgres pg_dump -U '$DB_USERNAME' '$DB_NAME' | gzip > "\$BACKUP_DIR/\$BACKUP_FILE"; then
    aws s3 cp "\$BACKUP_DIR/\$BACKUP_FILE" "s3://$BACKUP_S3_BUCKET_NAME/backups/postgres/\$TIMESTAMP/" --storage-class INTELLIGENT_TIERING
    rm -rf "\$BACKUP_DIR"
    echo "SUCCESS: Backup uploaded to S3 at \$(date)"
else
    echo "ERROR: Backup failed at \$(date)"
    exit 1
fi
EOF

chmod +x /home/ec2-user/db_backup_to_s3.sh
chown ec2-user:ec2-user /home/ec2-user/db_backup_to_s3.sh

# 9-2. systemd 서비스 등록 (User=root 권한으로 실행)
cat <<EOF > /etc/systemd/system/postgres-backup.service
[Unit]
Description=Daily PostgreSQL pg_dump Backup
After=docker.service

[Service]
Type=oneshot
User=root
ExecStart=/bin/bash /home/ec2-user/db_backup_to_s3.sh
EOF

# 9-3. systemd 타이머 등록 (Valkey와 겹치지 않게 1시간 차이 권장: 17:30 UTC = KST 새벽 2시 30분)
cat <<EOF > /etc/systemd/system/postgres-backup.timer
[Unit]
Description=Run PostgreSQL Backup daily at 2AM KST

[Timer]
OnCalendar=*-*-* 17:30:00
Persistent=true
Unit=postgres-backup.service

[Install]
WantedBy=timers.target
EOF

# 서비스 반영 및 타이머 활성화
systemctl daemon-reload
systemctl enable --now postgres-backup.timer

echo "INFO: PostgreSQL backup service and timer created successfully!"

# --- 10. PostGIS Extension 활성화 ---
sleep 30
echo "INFO: Enabling PostGIS extension..."
docker exec -e PGPASSWORD='$DB_PASSWORD' muroom-postgres psql -U "$DB_USERNAME" -d "$DB_NAME" -c "CREATE EXTENSION IF NOT EXISTS postgis;"
echo "INFO: PostGIS extension enabled."

echo "INFO: All Setup Completed Successfully!"