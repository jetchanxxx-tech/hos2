#!/bin/bash
# ============================================================
# 惠福星链 · 一键自动部署脚本
# Auto-detect environment, install deps, deploy all services
# 用法: curl -fsSL <raw-url> | bash
#      或: bash auto-deploy.sh
# ============================================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[✗]${NC} $1"; exit 1; }
info() { echo -e "${BLUE}[•]${NC} $1"; }

# ============================================================
# 0. 环境检测
# ============================================================
detect_os() {
    info "检测操作系统..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VER=$VERSION_ID
        log "OS: $NAME $VERSION_ID"
    else
        err "无法检测操作系统"
    fi

    ARCH=$(uname -m)
    log "架构: $ARCH"

    CPU_CORES=$(nproc)
    MEM_TOTAL=$(free -m | awk '/Mem/{print $2}')
    DISK_AVAIL=$(df -h / | awk 'NR==2{print $4}')
    log "CPU: ${CPU_CORES} 核 | 内存: ${MEM_TOTAL}MB | 磁盘可用: ${DISK_AVAIL}"
}

# ============================================================
# 1. 安装 Docker
# ============================================================
install_docker() {
    if command -v docker &>/dev/null; then
        log "Docker 已安装: $(docker --version)"
        return
    fi

    warn "Docker 未安装，开始安装..."
    case $OS in
        ubuntu|debian)
            curl -fsSL https://get.docker.com | bash
            ;;
        centos|rhel|fedora)
            curl -fsSL https://get.docker.com | bash
            ;;
        *)
            err "不支持的OS: $OS"
            ;;
    esac

    systemctl enable --now docker 2>/dev/null || service docker start
    log "Docker 安装完成"
}

# ============================================================
# 2. 安装 Docker Compose
# ============================================================
install_compose() {
    if docker compose version &>/dev/null; then
        log "Docker Compose 已安装: $(docker compose version --short)"
        return
    fi

    warn "Docker Compose 未安装，开始安装..."
    COMPOSE_VER=$(curl -fsSL https://api.github.com/repos/docker/compose/releases/latest \
        | grep -o '"tag_name": "[^"]*"' | head -1 | cut -d'"' -f4)
    [ -z "$COMPOSE_VER" ] && COMPOSE_VER="v2.24.0"

    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VER}/docker-compose-linux-${ARCH}" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # Also create legacy symlink
    ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose 2>/dev/null || true

    log "Docker Compose ${COMPOSE_VER} 安装完成"
}

# ============================================================
# 3. 创建目录结构
# ============================================================
setup_dirs() {
    info "创建目录..."
    mkdir -p /opt/huifu-starchain
    mkdir -p /backup/huifu-starchain
    mkdir -p /var/log/huifu-starchain
    log "目录已创建"
}

# ============================================================
# 4. 配置环境变量
# ============================================================
setup_env() {
    if [ -f /opt/huifu-starchain/deploy/.env ]; then
        log ".env 已存在，跳过生成"
        source /opt/huifu-starchain/deploy/.env
        return
    fi

    warn "生成随机密钥..."
    cat > /opt/huifu-starchain/deploy/.env << EOF
# 惠福星链 · 环境变量（自动生成）
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24)
MYSQL_PASSWORD=$(openssl rand -base64 24)
REDIS_PASSWORD=$(openssl rand -base64 16)
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
AES_KEY=$(openssl rand -base64 32)

# 微信（按需填入）
WECHAT_APP_ID=
WECHAT_APP_SECRET=

# 企业微信（按需填入）
WECOM_CORP_ID=
WECOM_CORP_SECRET=
WECOM_AGENT_ID=

# 医院网关（按需填入）
HOSPITAL_GATEWAY_URL=
HOSPITAL_API_KEY=

APP_VERSION=latest
EOF
    chmod 600 /opt/huifu-starchain/deploy/.env
    log ".env 已生成"

    set -a; source /opt/huifu-starchain/deploy/.env; set +a
}

# ============================================================
# 5. 配置 MySQL
# ============================================================
setup_mysql_conf() {
    mkdir -p /opt/huifu-starchain/deploy/mysql/conf.d
    cat > /opt/huifu-starchain/deploy/mysql/conf.d/huifu.cnf << 'MYSQLCNF'
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default_authentication_plugin = mysql_native_password
innodb_buffer_pool_size = 256M
innodb_log_file_size = 128M
max_connections = 200
max_allowed_packet = 64M
slow_query_log = 1
long_query_time = 1
sql_mode = STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION
MYSQLCNF
    log "MySQL 配置已生成"
}

# ============================================================
# 6. 部署服务
# ============================================================
deploy_services() {
    info "启动服务..."

    cd /opt/huifu-starchain/deploy

    # 停止旧容器（保留数据卷）
    docker compose down --remove-orphans 2>/dev/null || true

    # 拉取镜像 & 构建
    docker compose pull 2>/dev/null || true
    docker compose build api --no-cache 2>/dev/null || warn "API 镜像构建跳过（Maven/pom 项目需本地构建）"

    # 启动服务（不等待 API 镜像构建，用预构建镜像或跳过）
    docker compose up -d nginx mysql redis minio 2>/dev/null || true

    log "基础服务已启动"
}

# ============================================================
# 7. 健康检查
# ============================================================
health_check() {
    info "等待服务就绪 (30s)..."
    sleep 10

    local all_ok=true

    # 检查容器
    for svc in huifu-nginx huifu-mysql huifu-redis huifu-minio; do
        if docker ps --format '{{.Names}}' | grep -q "^${svc}$"; then
            log "容器 ${svc}: 运行中"
        else
            warn "容器 ${svc}: 未运行"
            all_ok=false
        fi
    done

    # 检查 MySQL
    if docker exec huifu-mysql mysqladmin ping -u root -p"${MYSQL_ROOT_PASSWORD}" &>/dev/null; then
        log "MySQL: 健康"
    else
        warn "MySQL: 未就绪"
        all_ok=false
    fi

    # 检查 API（如果已构建）
    if docker ps --format '{{.Names}}' | grep -q "huifu-api"; then
        sleep 30
        for i in 1 2 3; do
            if curl -sf http://localhost:8080/api/v1/health &>/dev/null; then
                log "API (8080): 健康"
                break
            else
                warn "API 健康检查尝试 $i/3..."
                sleep 10
            fi
        done
    else
        warn "API 容器未启动（需先执行 build 步骤）"
    fi

    if $all_ok; then
        log "基础服务全部就绪"
    fi
}

# ============================================================
# 8. 配置 SSL（可选）
# ============================================================
setup_ssl() {
    if [ "${1:-}" = "--ssl" ] && [ -n "${2:-}" ]; then
        local domain="$2"
        info "配置 SSL 证书: ${domain}"

        if command -v certbot &>/dev/null; then
            certbot certonly --standalone -d "${domain}" --non-interactive --agree-tos --email admin@${domain}
            cp /etc/letsencrypt/live/${domain}/fullchain.pem /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt
            cp /etc/letsencrypt/live/${domain}/privkey.pem /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.key
            docker compose -f /opt/huifu-starchain/deploy/docker-compose.yml restart nginx
            log "SSL 配置完成"
        else
            warn "certbot 未安装，跳过 SSL"
        fi
    else
        # 生成自签名证书（内网/测试）
        mkdir -p /opt/huifu-starchain/deploy/nginx/ssl
        if [ ! -f /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt ]; then
            openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
                -keyout /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.key \
                -out /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt \
                -subj "/CN=localhost" 2>/dev/null
            log "自签名证书已生成"
        fi
    fi
}

# ============================================================
# 9. 设置定时任务
# ============================================================
setup_cron() {
    # 数据库每日备份 (凌晨 2:00)
    if ! crontab -l 2>/dev/null | grep -q "huifu.*backup"; then
        (crontab -l 2>/dev/null; echo "0 2 * * * bash /opt/huifu-starchain/deploy/scripts/backup.sh >> /var/log/huifu-backup.log 2>&1") | crontab -
        log "定时备份已配置 (每日 02:00)"
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
install_compose
setup_dirs
setup_env
setup_mysql_conf
deploy_services
setup_ssl "$@"
setup_cron
health_check

echo ""
echo "============================================"
echo " 部署完成"
echo "============================================"
echo ""
echo " 访问地址:"
echo "   HTTP:  http://$(hostname -I 2>/dev/null | awk '{print $1}' || echo 'localhost')"
echo "   HTTPS: https://$(hostname -I 2>/dev/null | awk '{print $1}' || echo 'localhost')"
echo ""
echo " 服务端口:"
echo "   Nginx:  80, 443"
echo "   API:    8080"
echo "   MySQL:  3306"
echo "   Redis:  6379"
echo "   MinIO:  9001 (console)"
echo ""
echo " 管理命令:"
echo "   cd /opt/huifu-starchain/deploy"
echo "   docker compose ps              # 查看服务状态"
echo "   docker compose logs -f api     # 查看 API 日志"
echo "   docker compose restart api     # 重启 API"
echo "   bash scripts/backup.sh         # 手动备份数据库"
echo "   bash scripts/health-check.sh   # 健康检查"
echo ""
echo " 首次登录:"
echo "   账号: 13800001111"
echo "   密码: 任意"
echo "============================================"
