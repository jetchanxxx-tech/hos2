#!/bin/bash
# ============================================================
# 惠福星链 · 一键自动部署脚本（国内优化版）
# 用法: bash auto-deploy.sh
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log()  { echo -e "${GREEN}[√]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[X]${NC} $1"; }
info() { echo -e "${BLUE}[>]${NC} $1"; }

# ============================================================
# 0. 环境检测
# ============================================================
detect_os() {
    info "检测操作系统..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID; VER=$VERSION_ID
        log "OS: $NAME $VERSION_ID"
    else
        err "无法检测操作系统"
    fi
    ARCH=$(uname -m)
    CPU_CORES=$(nproc)
    MEM_TOTAL=$(free -m | awk '/Mem/{print $2}')
    DISK_AVAIL=$(df -h / | awk 'NR==2{print $4}')
    log "CPU: ${CPU_CORES} 核 | 内存: ${MEM_TOTAL}MB | 磁盘: ${DISK_AVAIL}"
}

# ============================================================
# 1. 配置 Docker 国内镜像加速（最先执行）
# ============================================================
setup_docker_mirror() {
    info "配置 Docker 国内镜像加速..."
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me",
    "https://docker.rainbond.cc",
    "https://dockerproxy.net",
    "https://docker.nju.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  }
}
EOF
    systemctl daemon-reload 2>/dev/null || true
    systemctl restart docker 2>/dev/null || service docker restart 2>/dev/null || true
    sleep 3
    if docker info &>/dev/null; then
        log "Docker 镜像加速配置完成"
    else
        warn "Docker 重启中，稍后重试..."
        sleep 5
    fi
}

# ============================================================
# 2. 安装 Docker（如果缺失）
# ============================================================
install_docker() {
    if command -v docker &>/dev/null; then
        log "Docker 已安装: $(docker --version)"
        return
    fi
    warn "安装 Docker..."
    curl -fsSL https://get.docker.com | bash
    systemctl enable --now docker 2>/dev/null || service docker start
    log "Docker 安装完成"
}

# ============================================================
# 3. 安装 Docker Compose 插件
# ============================================================
install_compose() {
    if docker compose version &>/dev/null; then
        log "Docker Compose 已安装: $(docker compose version --short)"
        return
    fi
    warn "安装 Docker Compose..."
    local v="v2.24.0"
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -fsSL "https://github.com/docker/compose/releases/download/${v}/docker-compose-linux-${ARCH}" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose 2>/dev/null || true
    log "Docker Compose ${v} 安装完成"
}

# ============================================================
# 4. 创建目录 & 生成密钥
# ============================================================
setup_dirs_and_env() {
    mkdir -p /opt/huifu-starchain/deploy/{mysql/conf.d,nginx/ssl,scripts}
    mkdir -p /backup/huifu-starchain
    mkdir -p /var/log/huifu-starchain

    if [ ! -f /opt/huifu-starchain/deploy/.env ]; then
        warn "生成随机密钥..."
        cat > /opt/huifu-starchain/deploy/.env << ENVEOF
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24)
MYSQL_PASSWORD=$(openssl rand -base64 24)
REDIS_PASSWORD=$(openssl rand -base64 16)
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
AES_KEY=$(openssl rand -base64 32)
WECHAT_APP_ID=
WECHAT_APP_SECRET=
WECOM_CORP_ID=
WECOM_CORP_SECRET=
HOSPITAL_GATEWAY_URL=
HOSPITAL_API_KEY=
APP_VERSION=latest
ENVEOF
        chmod 600 /opt/huifu-starchain/deploy/.env
        log ".env 已生成"
    fi

    # MySQL 配置
    cat > /opt/huifu-starchain/deploy/mysql/conf.d/huifu.cnf << 'MYSQLCNF'
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default_authentication_plugin = mysql_native_password
innodb_buffer_pool_size = 256M
max_connections = 100
max_allowed_packet = 64M
MYSQLCNF

    # SSL 自签名证书
    if [ ! -f /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.key \
            -out /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt \
            -subj "/CN=localhost" 2>/dev/null
        log "自签名证书已生成"
    fi

    set -a; source /opt/huifu-starchain/deploy/.env; set +a
}

# ============================================================
# 5. 拉取镜像（优先）
# ============================================================
pull_images() {
    info "预拉取 Docker 镜像（可能需要几分钟）..."
    local images=(
        "nginx:1.25-alpine"
        "mysql:8.0"
        "redis:7-alpine"
        "minio/minio:latest"
    )
    for img in "${images[@]}"; do
        info "拉取 $img ..."
        if docker pull "$img" 2>&1 | tail -1; then
            log "  $img OK"
        else
            warn "  $img 拉取失败，重试一次..."
            sleep 3
            docker pull "$img" 2>&1 | tail -1 || warn "  $img 再次失败，跳过"
        fi
    done
}

# ============================================================
# 6. 启动服务
# ============================================================
start_services() {
    info "启动服务..."
    cd /opt/huifu-starchain/deploy

    docker compose down --remove-orphans 2>/dev/null || true

    # 逐个启动，方便排查问题
    info "启动 MySQL..."
    docker compose up -d mysql 2>&1
    sleep 5

    info "启动 Redis..."
    docker compose up -d redis 2>&1
    sleep 2

    info "启动 MinIO..."
    docker compose up -d minio 2>&1
    sleep 3

    info "启动 Nginx..."
    docker compose up -d nginx 2>&1
    sleep 2

    log "基础服务启动完成"
}

# ============================================================
# 7. 初始化数据库
# ============================================================
init_database() {
    info "等待 MySQL 就绪..."
    for i in $(seq 1 20); do
        if docker exec huifu-mysql mysqladmin ping -u root -p"${MYSQL_ROOT_PASSWORD}" --silent 2>/dev/null; then
            log "MySQL 已就绪 (${i}s)"
            break
        fi
        [ "$i" -eq 20 ] && { warn "MySQL 启动超时"; return; }
        sleep 2
    done

    # 手动执行 SQL 初始化（因为 Flyway 依赖 API 容器）
    info "初始化数据库表..."
    if [ -f /opt/huifu-starchain/database/migrations/V1__init_schema.sql ]; then
        docker exec -i huifu-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" huifu_starchain \
            < /opt/huifu-starchain/database/migrations/V1__init_schema.sql 2>&1 | tail -3
        log "表结构已导入"
    fi
    if [ -f /opt/huifu-starchain/database/migrations/V2__seed_data.sql ]; then
        docker exec -i huifu-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" huifu_starchain \
            < /opt/huifu-starchain/database/migrations/V2__seed_data.sql 2>&1 | tail -3
        log "种子数据已导入"
    fi
}

# ============================================================
# 8. 部署后端 API
# ============================================================
deploy_api() {
    info "检查 API 部署方式..."

    local jar_path="/opt/huifu-starchain/backend/target/starchain-1.0.0-SNAPSHOT.jar"

    if [ -f "$jar_path" ]; then
        # 方式 A: 用 slim Dockerfile 构建镜像
        info "检测到预编译 jar，构建 Docker 镜像..."
        cd /opt/huifu-starchain
        docker build -f backend/Dockerfile.slim -t huifu-starchain-api:latest . 2>&1 | tail -5
        docker compose -f deploy/docker-compose.yml up -d api 2>&1
        log "API 服务已启动"
    elif command -v java &>/dev/null; then
        # 方式 B: 直接 java -jar 运行
        warn "未找到 jar，尝试 Maven 编译..."
        cd /opt/huifu-starchain/backend
        if [ -f pom.xml ]; then
            mvn clean package -DskipTests -q 2>&1 | tail -10
            cd /opt/huifu-starchain
            docker build -f backend/Dockerfile.slim -t huifu-starchain-api:latest . 2>&1 | tail -5
            docker compose -f deploy/docker-compose.yml up -d api 2>&1
            log "API 编译并启动完成"
        fi
    else
        warn "API 未部署（需要先编译 jar 或安装 Java/Maven）"
        warn "跳过 API，基础服务（MySQL/Redis/MinIO/Nginx）已正常运行"
        info ""
        info "=== 部署 API 的方法 ==="
        info "方案 A（推荐）: 在你本地编译 jar，然后 scp 上传到服务器"
        info "  # 本地:"
        info "  cd backend && mvn clean package -DskipTests"
        info "  scp target/starchain-1.0.0-SNAPSHOT.jar jet@server:/opt/huifu-starchain/backend/target/"
        info "  # 然后重新运行本脚本"
        info ""
        info "方案 B: 在服务器上安装 Java 21 + Maven，本脚本会自动编译"
        info "  apt install -y openjdk-21-jdk maven"
        info "  bash auto-deploy.sh"
    fi
}

# ============================================================
# 9. 健康检查
# ============================================================
health_check() {
    echo ""
    info "=== 健康检查 ==="

    local all_ok=true
    for svc in huifu-mysql huifu-redis huifu-minio huifu-nginx; do
        if docker ps --format '{{.Names}}' | grep -q "^${svc}$"; then
            log "容器 ${svc}: 运行中"
        else
            warn "容器 ${svc}: 未运行"
            all_ok=false
        fi
    done

    # MySQL 数据验证
    if docker exec huifu-mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='huifu_starchain';" 2>/dev/null; then
        log "数据库表已创建"
    fi

    # API 检查
    sleep 5
    if curl -sf http://localhost:8080/api/v1/health &>/dev/null; then
        log "API 服务: 健康"
    else
        warn "API 服务: 未运行（正常 — 如未部署 API）"
    fi
}

# ============================================================
# 10. 设置定时备份
# ============================================================
setup_cron() {
    local backup_script="/opt/huifu-starchain/deploy/scripts/backup.sh"
    if ! crontab -l 2>/dev/null | grep -q "backup"; then
        (crontab -l 2>/dev/null; echo "0 2 * * * bash ${backup_script} >> /var/log/huifu-backup.log 2>&1") | crontab -
        log "每日备份已配置 (02:00)"
    fi
}

# ============================================================
# Main
# ============================================================
echo ""
echo "============================================"
echo " 惠福星链 · 一键自动部署"
echo " Huifu StarChain · Auto Deploy"
echo " 时间: $(date)"
echo "============================================"
echo ""

detect_os
install_docker
setup_docker_mirror
install_compose
setup_dirs_and_env
pull_images
start_services
init_database
deploy_api
setup_cron
health_check

echo ""
echo "============================================"
echo " 部署完成"
echo "============================================"
echo ""
echo " 访问地址:  http://$(hostname -I 2>/dev/null | awk '{print $1}')"
echo ""
echo " 常用命令:"
echo "   docker compose -f /opt/huifu-starchain/deploy/docker-compose.yml ps"
echo "   docker compose -f /opt/huifu-starchain/deploy/docker-compose.yml logs -f"
echo "   bash /opt/huifu-starchain/deploy/scripts/backup.sh"
echo ""
echo " 首次登录: admin / admin123"
echo "============================================"
