#!/bin/bash
# ============================================================
# 惠福星链 · 数据库备份脚本
# 用法: crontab -e (建议每日凌晨 2:00 执行)
#   0 2 * * * /opt/huifu-starchain/deploy/scripts/backup.sh
# ============================================================
set -euo pipefail

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/huifu-starchain"
RETENTION_DAYS=30
MYSQL_CONTAINER="huifu-mysql"

mkdir -p "${BACKUP_DIR}"

# 加载环境变量
if [ -f "$(dirname "$0")/../.env" ]; then
    source "$(dirname "$0")/../.env"
fi

echo "[$(date)] 开始备份..."

# ---- MySQL 备份 ----
if docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    BACKUP_FILE="${BACKUP_DIR}/mysql_${TIMESTAMP}.sql.gz"
    docker exec "${MYSQL_CONTAINER}" mysqldump \
        -u root -p"${MYSQL_ROOT_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        huifu_starchain | gzip > "${BACKUP_FILE}"
    echo "  ✓ MySQL 备份: ${BACKUP_FILE}"
else
    echo "  ⚠ MySQL 容器未运行，跳过数据库备份"
fi

# ---- 清理过期备份 ----
find "${BACKUP_DIR}" -name "mysql_*.sql.gz" -mtime "+${RETENTION_DAYS}" -delete 2>/dev/null || true
echo "  ✓ 清理 ${RETENTION_DAYS} 天前的旧备份"

# ---- 备份大小统计 ----
echo ""
echo "  备份目录使用量:"
du -sh "${BACKUP_DIR}"

echo "[$(date)] 备份完成"
