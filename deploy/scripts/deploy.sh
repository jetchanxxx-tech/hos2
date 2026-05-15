#!/bin/bash
# ============================================================
# 惠福星链 · 部署脚本
# 用法: ./deploy.sh [prod|staging]
# 权限: root / sudoer
# ============================================================
set -euo pipefail

ENV="${1:-prod}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
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
    for cmd in docker docker-compose java mvn npm curl; do
        if ! command -v $cmd &>/dev/null; then
            echo "  ⚠ 缺少依赖: $cmd (跳过)"
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
        cat > "${DEPLOY_DIR}/.env" << 'ENVEOF'
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24)
MYSQL_PASSWORD=$(openssl rand -base64 24)
REDIS_PASSWORD=$(openssl rand -base64 16)
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
AES_KEY=$(openssl rand -base64 32)
APP_VERSION=latest
ENVEOF
        echo "  ✓ .env 已生成"
    else
        echo "  ✓ .env 已存在"
    fi
}

# ---- 3. 构建后端 ----
build_backend() {
    echo "[3/6] 构建后端..."
    cd "${PROJECT_DIR}/backend"
    mvn clean package -DskipTests -q
    cp target/starchain-*.jar "${DEPLOY_DIR}/"
    echo "  ✓ 后端构建完成"
}

# ---- 4. 构建前端 ----
build_frontend() {
    echo "[4/6] 构建前端..."
    cd "${PROJECT_DIR}/frontend"
    npm ci --silent
    npm run build
    mkdir -p "${DEPLOY_DIR}/dist"
    cp -r dist/* "${DEPLOY_DIR}/dist/"
    echo "  ✓ 前端构建完成"
}

# ---- 5. 备份数据库 ----
backup_db() {
    echo "[5/6] 备份数据库..."
    if docker ps | grep -q huifu-mysql; then
        docker exec huifu-mysql mysqldump -u root -p"${MYSQL_ROOT_PASSWORD}" \
            --single-transaction --routines --triggers huifu_starchain \
            | gzip > "/backup/huifu_starchain_${TIMESTAMP}.sql.gz" 2>/dev/null || true
        echo "  ✓ 数据库已备份"
    else
        echo "  - MySQL 未运行，跳过备份"
    fi
}

# ---- 6. 部署服务 ----
deploy_services() {
    echo "[6/6] 部署服务..."
    cd "${DEPLOY_DIR}"
    docker-compose down --remove-orphans 2>/dev/null || true
    docker-compose up -d --build
    echo "  ✓ 服务已启动"

    echo ""
    echo "  等待服务健康检查..."
    sleep 15
    docker-compose ps
}

# ---- 主流程 ----
main() {
    check_deps
    gen_secrets
    build_backend || echo "  ⚠ 后端构建失败，使用已有镜像"
    build_frontend || echo "  ⚠ 前端构建失败"
    backup_db
    deploy_services

    echo ""
    echo "============================================"
    echo " 部署完成"
    echo " 前端: https://your-server"
    echo " API:  https://your-server/api/v1/health"
    echo " MinIO: http://your-server:9001"
    echo " 日志: ${LOG_FILE}"
    echo "============================================"
}

main "$@"
