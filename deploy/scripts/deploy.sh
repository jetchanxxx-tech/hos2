#!/bin/bash
# ============================================================
# 惠福星链 · 部署脚本（原生部署版）
# 用法: ./deploy.sh [prod|staging]
# 权限: root / sudoer
# ============================================================
set -euo pipefail

ENV="${1:-prod}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"
DEPLOY_DIR="${PROJECT_DIR}/deploy"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="/var/log/huifu-starchain/deploy_${TIMESTAMP}.log"

mkdir -p /var/log/huifu-starchain
exec > >(tee -a "${LOG_FILE}") 2>&1

echo "============================================"
echo " 惠福星链 · 部署开始"
echo " 时间: $(date)"
echo " 环境: ${ENV}"
echo "============================================"

# ---- 1. 环境检查 ----
check_deps() {
    echo "[1/6] 检查依赖..."
    for cmd in java mvn npm systemctl curl; do
        if ! command -v $cmd &>/dev/null; then
            echo "  ✗ 缺少依赖: $cmd"
        else
            echo "  ✓ $cmd"
        fi
    done
}

# ---- 2. 生成密钥 ----
gen_secrets() {
    echo "[2/6] 检查密钥配置..."
    if [ ! -f "${DEPLOY_DIR}/.env" ]; then
        echo "  生成 .env 文件..."
        cat > "${DEPLOY_DIR}/.env" << ENVEOF
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DB=huifu_starchain
MYSQL_USER=huifu
REDIS_HOST=localhost
REDIS_PORT=6379
MINIO_HOST=localhost
MYSQL_ROOT_PASSWORD=nishi250
MYSQL_PASSWORD=nishi250
REDIS_PASSWORD=$(openssl rand -base64 16)
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
AES_KEY=$(openssl rand -base64 32)
APP_VERSION=1.0.0
SPRING_PROFILES_ACTIVE=prod
ENVEOF
        chmod 600 "${DEPLOY_DIR}/.env"
        echo "  ✓ .env 已生成"
    else
        echo "  ✓ .env 已存在"
    fi
}

# 加载 .env
if [ -f "${DEPLOY_DIR}/.env" ]; then
    set -a; source "${DEPLOY_DIR}/.env"; set +a
fi

# ---- 3. 构建后端 ----
build_backend() {
    echo "[3/6] 构建后端..."
    cd "${PROJECT_DIR}/backend"
    mvn clean package -DskipTests -q
    # 创建版本无关 symlink
    ln -sf starchain-1.0.0-SNAPSHOT.jar target/starchain-1.0.0.jar
    echo "  ✓ 后端构建完成"
}

# ---- 4. 构建前端 ----
build_frontend() {
    echo "[4/6] 构建前端..."
    cd "${PROJECT_DIR}/frontend"
    npm ci --silent
    npm run build
    echo "  ✓ 前端构建完成"
}

# ---- 5. 备份数据库 ----
backup_db() {
    echo "[5/6] 备份数据库..."
    if [ -f "${DEPLOY_DIR}/scripts/backup.sh" ]; then
        bash "${DEPLOY_DIR}/scripts/backup.sh" || echo "  - 备份跳过"
    else
        echo "  - 备份脚本未找到，跳过"
    fi
}

# ---- 6. 部署服务 ----
deploy_services() {
    echo "[6/6] 部署服务..."

    # 部署 nginx 配置
    if [ -f "${DEPLOY_DIR}/nginx/nginx.conf" ]; then
        cp "${DEPLOY_DIR}/nginx/nginx.conf" /etc/nginx/nginx.conf
        nginx -t && systemctl reload nginx 2>/dev/null || systemctl start nginx 2>/dev/null || true
        echo "  ✓ Nginx 配置已更新"
    fi

    # 部署 systemd unit
    if [ -f "${DEPLOY_DIR}/systemd/huifu-api.service" ]; then
        cp "${DEPLOY_DIR}/systemd/huifu-api.service" /etc/systemd/system/huifu-api.service
        systemctl daemon-reload
        echo "  ✓ Systemd unit 已更新"
    fi

    # 确保 huifu 用户存在
    id -u huifu &>/dev/null || useradd -r -s /sbin/nologin -d /opt/huifu-starchain huifu

    # 设置权限
    mkdir -p /opt/huifu-starchain
    cp -r "${PROJECT_DIR}/backend" /opt/huifu-starchain/ 2>/dev/null || true
    mkdir -p /opt/huifu-starchain/frontend/dist
    cp -r "${PROJECT_DIR}/frontend/dist"/* /opt/huifu-starchain/frontend/dist/ 2>/dev/null || true
    chown -R huifu:huifu /opt/huifu-starchain
    chown -R huifu:huifu /var/log/huifu-starchain 2>/dev/null || true

    # 重启 API
    systemctl restart huifu-api 2>/dev/null || systemctl start huifu-api 2>/dev/null || echo "  - API 服务启动跳过（首次部署请运行 auto-deploy.sh）"

    echo "  ✓ 服务已部署"

    echo ""
    echo "  等待服务启动..."
    sleep 10
    systemctl status huifu-api --no-pager 2>/dev/null | head -10 || true
    systemctl status nginx --no-pager 2>/dev/null | head -5 || true
}

# ---- 主流程 ----
main() {
    check_deps
    gen_secrets
    build_backend || echo "  ✗ 后端构建失败"
    build_frontend || echo "  ✗ 前端构建失败"
    backup_db
    deploy_services

    echo ""
    echo "============================================"
    echo " 部署完成"
    echo " 前端: http://$(hostname -I 2>/dev/null | awk '{print $1}')"
    echo " API:  http://$(hostname -I 2>/dev/null | awk '{print $1}')/api/v1/health"
    echo " MinIO: http://$(hostname -I 2>/dev/null | awk '{print $1}'):9001"
    echo " 日志: ${LOG_FILE}"
    echo ""
    echo " 常用命令:"
    echo "   sudo systemctl status huifu-api"
    echo "   sudo journalctl -u huifu-api -f"
    echo "   sudo systemctl reload nginx"
    echo "   bash /opt/huifu-starchain/deploy/scripts/backup.sh"
    echo "============================================"
}

main "$@"
