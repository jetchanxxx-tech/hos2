#!/bin/bash
# ============================================================
# 惠福星链 · 健康检查脚本
# 用法: ./health-check.sh
# 返回: 0=全部健康, 1=部分异常
# ============================================================
set -euo pipefail

API_URL="${API_URL:-http://localhost}"
EXIT_CODE=0

check() {
    local name="$1" url="$2"
    local status=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 --max-time 10 "${url}" 2>/dev/null || echo "000")
    if [ "$status" = "200" ]; then
        echo "  ✓ ${name}: ${status}"
    else
        echo "  ✗ ${name}: ${status} (FAIL)"
        EXIT_CODE=1
    fi
}

echo "============================================"
echo " 惠福星链 · 健康检查"
echo " 时间: $(date)"
echo "============================================"

echo "[API]"
check "  Health"    "${API_URL}/api/v1/health"
check "  Dashboard" "${API_URL}/api/v1/dashboard/kpi-summary"

echo "[Docker]"
for container in huifu-nginx huifu-api huifu-mysql huifu-redis huifu-minio; do
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        status=$(docker inspect -f '{{.State.Status}}' "${container}" 2>/dev/null)
        if [ "$status" = "running" ]; then
            echo "  ✓ ${container}: ${status}"
        else
            echo "  ✗ ${container}: ${status}"
            EXIT_CODE=1
        fi
    else
        echo "  - ${container}: not found"
    fi
done

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
