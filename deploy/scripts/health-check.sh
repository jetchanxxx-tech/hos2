#!/bin/bash
# ============================================================
# 惠福星链 · 健康检查脚本（原生部署版）
# 用法: ./health-check.sh
# 返回: 0=全部健康, 1=部分异常
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
API_URL="${API_URL:-http://localhost}"
EXIT_CODE=0

# 加载环境变量
if [ -f "${SCRIPT_DIR}/../.env" ]; then
    set -a; source "${SCRIPT_DIR}/../.env"; set +a
fi

check_http() {
    local name="$1" url="$2"
    local status=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 --max-time 10 "${url}" 2>/dev/null || echo "000")
    if [ "$status" = "200" ]; then
        echo "  ✓ ${name}: ${status}"
    else
        echo "  ✗ ${name}: ${status} (FAIL)"
        EXIT_CODE=1
    fi
}

check_systemd() {
    local name="$1" svc="$2"
    if systemctl is-active --quiet "${svc}" 2>/dev/null; then
        echo "  ✓ ${name}: running"
    elif pgrep -f "${svc}" &>/dev/null; then
        echo "  ✓ ${name}: running (process detected)"
    else
        echo "  ✗ ${name}: NOT running"
        EXIT_CODE=1
    fi
}

echo "============================================"
echo " 惠福星链 · 健康检查"
echo " 时间: $(date)"
echo "============================================"

echo "[API]"
check_http "  Health"     "${API_URL}/api/v1/health"
check_http "  Dashboard"  "${API_URL}/api/v1/dashboard/kpi-summary"
check_http "  Nginx->API" "http://localhost/api/v1/health"

echo "[Services]"
check_systemd "  nginx"    "nginx"
check_systemd "  huifu-api" "huifu-api"

# MySQL
if [ "${MYSQL_HOST:-localhost}" = "localhost" ]; then
    if systemctl is-active --quiet mysql 2>/dev/null || systemctl is-active --quiet mysqld 2>/dev/null; then
        echo "  ✓ mysql: running"
        # 连接测试
        if mysqladmin ping -u root -p"${MYSQL_ROOT_PASSWORD:-}" --silent 2>/dev/null; then
            echo "  ✓ mysql: connectable"
        else
            echo "  ✗ mysql: connection failed"
            EXIT_CODE=1
        fi
    else
        echo "  ✗ mysql: NOT running"
        EXIT_CODE=1
    fi
else
    echo "  - mysql: remote host (${MYSQL_HOST}), skipped local check"
fi

# Redis
if [ "${REDIS_HOST:-localhost}" = "localhost" ]; then
    check_systemd "  redis" "redis"
else
    echo "  - redis: remote host (${REDIS_HOST}), skipped local check"
fi

# MinIO
if [ "${MINIO_HOST:-localhost}" = "localhost" ]; then
    check_systemd "  minio" "minio"
else
    echo "  - minio: remote host (${MINIO_HOST}), skipped local check"
fi

echo "[资源]"
echo "  磁盘: $(df -h / | awk 'NR==2{print $5}') 已使用"
if command -v free &>/dev/null; then
    echo "  内存: $(free -h | awk '/Mem/{print $3"/"$2}')"
fi
echo "  系统负载: $(uptime | awk -F'load average:' '{print $2}')"

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "✓ 所有服务健康"
else
    echo "✗ 部分服务异常，请检查日志"
fi

exit $EXIT_CODE
