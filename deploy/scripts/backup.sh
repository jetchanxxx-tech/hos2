#!/bin/bash
# ============================================================
# 惠福星链 · 数据库备份脚本（原生部署版）
# 用法: crontab -e (建议每日凌晨 2:00 执行)
#   0 2 * * * /opt/huifu-starchain/deploy/scripts/backup.sh
# ============================================================
set -euo pipefail

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/huifu-starchain"
RETENTION_DAYS=30

mkdir -p "${BACKUP_DIR}"

# 加载环境变量
if [ -f "$(dirname "$0")/../.env" ]; then
    set -a; source "$(dirname "$0")/../.env"; set +a
fi

MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-huifu}"
MYSQL_DB="${MYSQL_DB:-huifu_starchain}"

echo "[$(date)] 开始备份..."

# ---- MySQL 备份 ----
BACKUP_FILE="${BACKUP_DIR}/mysql_${TIMESTAMP}.sql.gz"

if [ "${MYSQL_HOST}" = "localhost" ]; then
    # 本地备份
    if systemctl is-active --quiet mysql 2>/dev/null || systemctl is-active --quiet mysqld 2>/dev/null; then
        mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
            --single-transaction \
            --routines \
            --triggers \
            --events \
            --hex-blob \
            "${MYSQL_DB}" | gzip > "${BACKUP_FILE}"
        echo "  ✓ MySQL 备份 (local): ${BACKUP_FILE}"
    else
        echo "  ✗ MySQL 未运行，跳过数据库备份"
        exit 0
    fi
else
    # 远程备份
    mysqldump -h "${MYSQL_HOST}" -P "${MYSQL_PORT}" \
        -u "${MYSQL_USER}" -p"${MYSQL_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        "${MYSQL_DB}" | gzip > "${BACKUP_FILE}"
    echo "  ✓ MySQL 备份 (remote ${MYSQL_HOST}): ${BACKUP_FILE}"
fi

# ---- 清理过期备份 ----
find "${BACKUP_DIR}" -name "mysql_*.sql.gz" -mtime "+${RETENTION_DAYS}" -delete 2>/dev/null || true
echo "  ✓ 清理 ${RETENTION_DAYS} 天前的旧备份"

# ---- 备份大小统计 ----
echo ""
echo "  备份目录使用量:"
du -sh "${BACKUP_DIR}"

echo "[$(date)] 备份完成"
