#!/bin/bash
# ============================================================
# 惠福星链 · 一键自动部署脚本（原生部署版）
# 用法: bash auto-deploy.sh [--incremental] [--skip-build] [--skip-db]
# ============================================================
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log()  { echo -e "${GREEN}[√]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
err()  { echo -e "${RED}[X]${NC} $1"; }
info() { echo -e "${BLUE}[>]${NC} $1"; }

# ============================================================
# 常量
# ============================================================
PROJECT_ROOT="/opt/huifu-starchain"
DEPLOY_DIR="${PROJECT_ROOT}/deploy"
BACKEND_DIR="${PROJECT_ROOT}/backend"
FRONTEND_DIR="${PROJECT_ROOT}/frontend"
DIST_DIR="${FRONTEND_DIR}/dist"
NGINX_CONF_SRC="${DEPLOY_DIR}/nginx/nginx.conf"
SYSTEMD_DIR="${DEPLOY_DIR}/systemd"
ENV_FILE="${DEPLOY_DIR}/.env"
LOG_DIR="/var/log/huifu-starchain"
BACKUP_DIR="/backup/huifu-starchain"

# ============================================================
# 命令行参数解析
# ============================================================
INCREMENTAL=false
SKIP_BUILD=false
SKIP_DB=false

for arg in "$@"; do
    case $arg in
        --incremental) INCREMENTAL=true ;;
        --skip-build)  SKIP_BUILD=true ;;
        --skip-db)     SKIP_DB=true ;;
        *) warn "未知参数: $arg" ;;
    esac
done

# ============================================================
# 0. 环境检测
# ============================================================
detect_os() {
    info "检测操作系统..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        OS_NAME=$NAME
        VER=$VERSION_ID
        log "OS: $NAME $VERSION_ID"
    else
        err "无法检测操作系统"; exit 1
    fi

    case $OS in
        ubuntu|debian)
            OS_FAMILY="debian"
            PKG_MGR="apt-get"
            ;;
        centos|rhel|rocky|almalinux|fedora|tencentos|anolis|openEuler)
            OS_FAMILY="rhel"
            PKG_MGR=$(command -v dnf &>/dev/null && echo "dnf" || echo "yum")
            ;;
        *)
            warn "未知发行版: $OS，将尝试通用安装"
            OS_FAMILY="unknown"
            PKG_MGR=""
            ;;
    esac

    ARCH=$(uname -m)
    CPU_CORES=$(nproc)
    MEM_TOTAL=$(free -m | awk '/Mem/{print $2}')
    DISK_AVAIL=$(df -h / | awk 'NR==2{print $4}')
    log "CPU: ${CPU_CORES} 核 | 内存: ${MEM_TOTAL}MB | 磁盘: ${DISK_AVAIL}"
}

check_root() {
    if [ "$(id -u)" != "0" ]; then
        err "请使用 root 权限运行: sudo bash auto-deploy.sh"
        exit 1
    fi
}

load_env() {
    if [ -f "${ENV_FILE}" ]; then
        set -a; source "${ENV_FILE}"; set +a
        log "已加载 .env 配置"
    fi
}

# ============================================================
# 1. 组件检测函数
# ============================================================

check_java() {
    if command -v java &>/dev/null; then
        local ver=$(java -version 2>&1 | head -1 | grep -oP 'version "\K[^"]+' | cut -d. -f1)
        if [ "${ver}" -ge 21 ] 2>/dev/null; then
            log "Java $(java -version 2>&1 | head -1 | grep -oP '\d+\.\d+\.\d+') 已安装"
            return 0
        fi
    fi
    warn "Java 21 未安装"
    return 1
}

check_maven() {
    if command -v mvn &>/dev/null; then
        log "Maven $(mvn --version 2>/dev/null | head -1 | awk '{print $3}') 已安装"
        return 0
    fi
    warn "Maven 未安装"
    return 1
}

check_nodejs() {
    if command -v node &>/dev/null; then
        local ver=$(node -v | grep -oP '\d+' | head -1)
        if [ "${ver}" -ge 18 ] 2>/dev/null; then
            log "Node.js $(node -v) 已安装"
            return 0
        fi
    fi
    warn "Node.js 18+ 未安装"
    return 1
}

check_mysql() {
    if [ "${MYSQL_HOST:-localhost}" != "localhost" ]; then
        log "MySQL 远程主机: ${MYSQL_HOST}，跳过本地检测"
        return 0
    fi
    if command -v mysql &>/dev/null; then
        log "MySQL 客户端已安装"
        return 0
    fi
    warn "MySQL 未安装"
    return 1
}

check_redis() {
    if [ "${REDIS_HOST:-localhost}" != "localhost" ]; then
        log "Redis 远程主机: ${REDIS_HOST}，跳过本地检测"
        return 0
    fi
    if command -v redis-server &>/dev/null || command -v redis-cli &>/dev/null; then
        log "Redis 已安装"
        return 0
    fi
    warn "Redis 未安装"
    return 1
}

check_minio() {
    if [ "${MINIO_HOST:-localhost}" != "localhost" ]; then
        log "MinIO 远程主机: ${MINIO_HOST}，跳过本地检测"
        return 0
    fi
    if command -v minio &>/dev/null; then
        log "MinIO 已安装"
        return 0
    fi
    warn "MinIO 未安装"
    return 1
}

# ============================================================
# 2. 组件安装函数
# ============================================================

install_java() {
    check_java && return 0
    info "安装 Java 21 JDK..."
    case $OS_FAMILY in
        debian)
            apt-get update -qq
            apt-get install -y -qq openjdk-21-jdk || {
                err "Java 21 安装失败。请手动安装: apt install openjdk-21-jdk"; return 1
            }
            ;;
        rhel)
            if command -v dnf &>/dev/null; then
                dnf install -y -q java-21-openjdk-devel || {
                    err "Java 21 安装失败"; return 1
                }
            else
                yum install -y -q java-21-openjdk-devel || {
                    err "Java 21 安装失败"; return 1
                }
            fi
            ;;
        *) err "不支持自动安装 Java，请手动安装 Java 21 JDK"; return 1 ;;
    esac
    check_java || { err "Java 安装后验证失败"; return 1; }
    log "Java 21 JDK 安装完成"
}

install_maven() {
    check_maven && return 0
    info "安装 Maven..."
    case $OS_FAMILY in
        debian)
            apt-get install -y -qq maven 2>/dev/null || {
                # 发行版 Maven 太旧，下载最新版
                info "发行版 Maven 不可用，下载 Apache Maven..."
                local mv="3.9.9"
                curl -fsSL "https://dlcdn.apache.org/maven/maven-3/${mv}/binaries/apache-maven-${mv}-bin.tar.gz" \
                    -o /tmp/maven.tar.gz
                tar -xzf /tmp/maven.tar.gz -C /opt
                ln -sf /opt/apache-maven-${mv}/bin/mvn /usr/local/bin/mvn
                rm -f /tmp/maven.tar.gz
            }
            ;;
        rhel)
            if command -v dnf &>/dev/null; then
                dnf install -y -q maven 2>/dev/null || {
                    info "下载 Apache Maven..."
                    local mv="3.9.9"
                    curl -fsSL "https://dlcdn.apache.org/maven/maven-3/${mv}/binaries/apache-maven-${mv}-bin.tar.gz" \
                        -o /tmp/maven.tar.gz
                    tar -xzf /tmp/maven.tar.gz -C /opt
                    ln -sf /opt/apache-maven-${mv}/bin/mvn /usr/local/bin/mvn
                    rm -f /tmp/maven.tar.gz
                }
            else
                yum install -y -q maven 2>/dev/null || true
            fi
            ;;
        *) warn "请手动安装 Maven: https://maven.apache.org/download.cgi" ;;
    esac
    check_maven || warn "Maven 安装后未检测到，编译前请确保 mvn 可用"
}

install_nodejs() {
    check_nodejs && return 0
    info "安装 Node.js 20.x..."
    case $OS_FAMILY in
        debian)
            curl -fsSL https://deb.nodesource.com/setup_20.x | bash - 2>/dev/null
            apt-get install -y -qq nodejs || {
                err "Node.js 安装失败"; return 1
            }
            ;;
        rhel)
            curl -fsSL https://rpm.nodesource.com/setup_20.x | bash - 2>/dev/null
            if command -v dnf &>/dev/null; then
                dnf install -y -q nodejs || { err "Node.js 安装失败"; return 1; }
            else
                yum install -y -q nodejs || { err "Node.js 安装失败"; return 1; }
            fi
            ;;
        *) err "不支持自动安装 Node.js，请手动安装 Node.js 18+"; return 1 ;;
    esac
    check_nodejs || { err "Node.js 安装后验证失败"; return 1; }
    log "Node.js 安装完成"
}

install_mysql() {
    check_mysql && return 0
    info "安装 MySQL 8.0..."
    case $OS_FAMILY in
        debian)
            apt-get update -qq
            apt-get install -y -qq mysql-server-8.0 || apt-get install -y -qq mysql-server || {
                warn "MySQL 安装失败，将尝试默认包..."
                apt-get install -y -qq default-mysql-server || { err "MySQL 安装失败"; return 1; }
            }
            ;;
        rhel)
            if command -v dnf &>/dev/null; then
                dnf install -y -q mysql-server || { err "MySQL 安装失败"; return 1; }
            else
                yum install -y -q mysql-server || { err "MySQL 安装失败"; return 1; }
            fi
            ;;
        *) err "不支持自动安装 MySQL"; return 1 ;;
    esac

    # 配置 MySQL
    local cnf_dir=""
    case $OS_FAMILY in
        debian) cnf_dir="/etc/mysql/conf.d" ;;
        rhel)   cnf_dir="/etc/my.cnf.d" ;;
    esac
    if [ -n "$cnf_dir" ]; then
        mkdir -p "$cnf_dir"
        cat > "${cnf_dir}/huifu.cnf" << 'MYSQLCNF'
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default_authentication_plugin = mysql_native_password
innodb_buffer_pool_size = 256M
max_connections = 100
max_allowed_packet = 64M
MYSQLCNF
    fi

    systemctl enable --now mysql 2>/dev/null || systemctl enable --now mysqld 2>/dev/null || true
    log "MySQL 安装完成"
}

install_redis() {
    check_redis && return 0
    info "安装 Redis..."
    case $OS_FAMILY in
        debian)
            apt-get update -qq
            apt-get install -y -qq redis-server || { err "Redis 安装失败"; return 1; }
            ;;
        rhel)
            if command -v dnf &>/dev/null; then
                dnf install -y -q epel-release 2>/dev/null || true
                dnf install -y -q redis || { err "Redis 安装失败"; return 1; }
            else
                yum install -y -q epel-release 2>/dev/null || true
                yum install -y -q redis || { err "Redis 安装失败"; return 1; }
            fi
            ;;
        *) err "不支持自动安装 Redis"; return 1 ;;
    esac

    # 配置 Redis 密码
    if [ -n "${REDIS_PASSWORD:-}" ]; then
        sed -i "s/^# requirepass .*/requirepass ${REDIS_PASSWORD}/" /etc/redis/redis.conf 2>/dev/null || \
            sed -i "s/^# requirepass .*/requirepass ${REDIS_PASSWORD}/" /etc/redis.conf 2>/dev/null || true
    fi

    systemctl enable --now redis 2>/dev/null || systemctl enable --now redis-server 2>/dev/null || true
    log "Redis 安装完成"
}

install_minio() {
    check_minio && return 0
    info "安装 MinIO..."
    curl -fsSL -o /usr/local/bin/minio https://dl.min.io/server/minio/release/linux-amd64/minio
    chmod +x /usr/local/bin/minio

    # 创建 minio 用户
    id -u minio &>/dev/null || useradd -r -s /sbin/nologin -d /var/lib/minio minio
    mkdir -p /var/lib/minio/data /etc/minio
    chown -R minio:minio /var/lib/minio

    # MinIO 配置
    cat > /etc/minio/minio.conf << MINIOCNF
MINIO_ROOT_USER=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY:-minioadmin}
MINIOCNF
    chmod 600 /etc/minio/minio.conf

    # MinIO systemd unit
    cat > /etc/systemd/system/minio.service << 'MINIOSVC'
[Unit]
Description=MinIO Object Storage
After=network.target

[Service]
Type=simple
User=minio
Group=minio
EnvironmentFile=-/etc/minio/minio.conf
ExecStart=/usr/local/bin/minio server /var/lib/minio/data --console-address ":9001"
Restart=on-failure
RestartSec=5
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
MINIOSVC

    systemctl daemon-reload
    systemctl enable --now minio 2>/dev/null || true
    log "MinIO 安装完成"
}

# ============================================================
# 3. 目录初始化 & 密钥生成
# ============================================================
setup_dirs_and_env() {
    info "初始化目录结构..."
    mkdir -p "${PROJECT_ROOT}"/{backend/target,frontend/dist,deploy/{nginx/ssl,scripts}}
    mkdir -p "${LOG_DIR}" "${BACKUP_DIR}"

    if [ ! -f "${ENV_FILE}" ]; then
        warn "生成随机密钥..."
        cat > "${ENV_FILE}" << ENVEOF
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
WECHAT_APP_ID=
WECHAT_APP_SECRET=
WECOM_CORP_ID=
WECOM_CORP_SECRET=
WECOM_AGENT_ID=
HOSPITAL_GATEWAY_URL=http://10.0.0.100:8081
HOSPITAL_API_KEY=
APP_VERSION=latest
SPRING_PROFILES_ACTIVE=prod
ENVEOF
        chmod 600 "${ENV_FILE}"
        log ".env 已生成"
        set -a; source "${ENV_FILE}"; set +a
    fi

    # SSL 自签名证书
    if [ ! -f "${DEPLOY_DIR}/nginx/ssl/huifu-starchain.crt" ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout "${DEPLOY_DIR}/nginx/ssl/huifu-starchain.key" \
            -out "${DEPLOY_DIR}/nginx/ssl/huifu-starchain.crt" \
            -subj "/CN=localhost" 2>/dev/null
        log "自签名证书已生成"
    fi

    # 创建 huifu 系统用户
    id -u huifu &>/dev/null || useradd -r -s /sbin/nologin -d "${PROJECT_ROOT}" huifu
}

# ============================================================
# 4. 构建
# ============================================================
build_backend() {
    info "构建后端..."
    cd "${BACKEND_DIR}"
    if [ ! -f pom.xml ]; then
        err "pom.xml 未找到，跳过后端构建"
        return 1
    fi
    mvn clean package -DskipTests -q
    # 版本无关 symlink
    ln -sf target/starchain-1.0.0-SNAPSHOT.jar target/starchain-1.0.0.jar
    log "后端构建完成: $(ls -lh target/starchain-1.0.0-SNAPSHOT.jar | awk '{print $5}')"
}

build_frontend() {
    info "构建前端..."
    cd "${FRONTEND_DIR}"
    if [ ! -f package.json ]; then
        err "package.json 未找到，跳过前端构建"
        return 1
    fi
    npm ci --silent 2>/dev/null || npm install --silent
    npm run build
    log "前端构建完成"
}

# ============================================================
# 5. 数据库初始化
# ============================================================
init_database() {
    if [ "${MYSQL_HOST:-localhost}" != "localhost" ]; then
        info "MySQL 远程主机 (${MYSQL_HOST})，跳过本地数据库初始化"
        return 0
    fi

    info "等待 MySQL 就绪..."
    for i in $(seq 1 30); do
        if mysqladmin ping -u root --silent 2>/dev/null; then
            log "MySQL 已就绪 (${i}s)"
            break
        fi
        # RHEL 可能使用 socket auth
        if [ -S /var/lib/mysql/mysql.sock ]; then
            if mysql -u root -e "SELECT 1" &>/dev/null; then
                log "MySQL 已就绪 (socket auth)"
                break
            fi
        fi
        [ "$i" -eq 30 ] && { err "MySQL 启动超时"; return 1; }
        sleep 2
    done

    # 确定连接方式
    local MYSQL_CMD="mysql -u root"
    if [ -n "${MYSQL_ROOT_PASSWORD:-}" ]; then
        MYSQL_CMD="mysql -u root -p${MYSQL_ROOT_PASSWORD}"
    fi

    info "初始化数据库..."
    $MYSQL_CMD -e "CREATE DATABASE IF NOT EXISTS huifu_starchain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
    $MYSQL_CMD -e "CREATE USER IF NOT EXISTS 'huifu'@'localhost' IDENTIFIED BY '${MYSQL_PASSWORD}';" 2>/dev/null
    $MYSQL_CMD -e "GRANT ALL PRIVILEGES ON huifu_starchain.* TO 'huifu'@'localhost'; FLUSH PRIVILEGES;" 2>/dev/null
    log "数据库初始化完成（Flyway 迁移由 API 启动时自动执行）"
}

# ============================================================
# 6. 配置部署
# ============================================================
deploy_systemd_units() {
    info "部署 Systemd 配置..."
    if [ -f "${SYSTEMD_DIR}/huifu-api.service" ]; then
        cp "${SYSTEMD_DIR}/huifu-api.service" /etc/systemd/system/huifu-api.service
    fi
    systemctl daemon-reload
    log "Systemd 配置已更新"
}

# ============================================================
# 7. 服务启动
# ============================================================
wait_for_port() {
    local host="$1" port="$2" timeout="$3" desc="$4"
    info "等待 ${desc} (${host}:${port})..."
    for i in $(seq 1 "$timeout"); do
        if timeout 2 bash -c "echo >/dev/tcp/${host}/${port}" 2>/dev/null; then
            log "${desc} 已就绪 (${i}s)"
            return 0
        fi
        sleep 1
    done
    warn "${desc} 就绪超时 (${timeout}s)"
    return 1
}

start_services() {
    info "按依赖顺序启动服务..."

    # 1. MySQL
    if [ "${MYSQL_HOST:-localhost}" = "localhost" ]; then
        systemctl enable --now mysql 2>/dev/null || systemctl enable --now mysqld 2>/dev/null || true
        wait_for_port 127.0.0.1 3306 30 "MySQL" || true
    fi

    # 2. Redis
    if [ "${REDIS_HOST:-localhost}" = "localhost" ]; then
        systemctl enable --now redis 2>/dev/null || systemctl enable --now redis-server 2>/dev/null || true
        wait_for_port 127.0.0.1 6379 15 "Redis" || true
    fi

    # 3. MinIO
    if [ "${MINIO_HOST:-localhost}" = "localhost" ]; then
        systemctl enable --now minio 2>/dev/null || true
        wait_for_port 127.0.0.1 9000 15 "MinIO" || true
    fi

    # 4. API
    info "启动 API 服务..."
    chown -R huifu:huifu "${PROJECT_ROOT}" 2>/dev/null || true
    chown -R huifu:huifu "${LOG_DIR}" 2>/dev/null || true

    # SELinux context
    if command -v chcon &>/dev/null; then
        chcon -R -t httpd_sys_content_t "${FRONTEND_DIR}/dist" 2>/dev/null || true
    fi

    systemctl enable --now huifu-api 2>/dev/null || true
    wait_for_port 127.0.0.1 8080 120 "API"

    log "所有服务启动完成"
}

# ============================================================
# 8. 定时备份
# ============================================================
setup_cron() {
    local backup_script="${DEPLOY_DIR}/scripts/backup.sh"
    if [ -f "$backup_script" ]; then
        if ! crontab -l 2>/dev/null | grep -q "backup.sh"; then
            (crontab -l 2>/dev/null; echo "0 2 * * * bash ${backup_script} >> /var/log/huifu-backup.log 2>&1") | crontab -
            log "每日备份已配置 (02:00)"
        fi
    fi
}

# ============================================================
# 9. 健康检查
# ============================================================
health_check() {
    echo ""
    info "=== 健康检查 ==="

    local all_ok=true

    check_svc() {
        local name="$1" svc="$2"
        if systemctl is-active --quiet "${svc}" 2>/dev/null; then
            log "服务 ${name}: 运行中"
        else
            warn "服务 ${name}: 未运行"
            all_ok=false
        fi
    }

    # 本地服务检查
    if [ "${MYSQL_HOST:-localhost}" = "localhost" ]; then
        check_svc "MySQL" "mysql" || check_svc "MySQL" "mysqld"
    fi
    if [ "${REDIS_HOST:-localhost}" = "localhost" ]; then
        check_svc "Redis" "redis" || check_svc "Redis" "redis-server"
    fi
    if [ "${MINIO_HOST:-localhost}" = "localhost" ]; then
        check_svc "MinIO" "minio"
    fi
    check_svc "Nginx" "nginx"
    check_svc "API" "huifu-api"

    # 数据库连接验证
    echo ""
    if [ "${MYSQL_HOST:-localhost}" = "localhost" ]; then
        if mysql -u root -p"${MYSQL_ROOT_PASSWORD:-}" -e "SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='huifu_starchain';" 2>/dev/null; then
            log "数据库表已创建"
        else
            warn "数据库验证失败（API 启动后将由 Flyway 自动迁移）"
        fi
    fi

    # API 端点检查
    sleep 3
    if curl -sf http://localhost:8080/api/v1/health &>/dev/null; then
        log "API /health: 200 OK"
    else
        warn "API /health: 未就绪（Flyway 迁移可能需要 1-2 分钟）"
    fi

    if curl -sf http://localhost/api/v1/health &>/dev/null; then
        log "Nginx->API 代理: 200 OK"
    fi
}

# ============================================================
# 10. 部署摘要
# ============================================================
print_summary() {
    local ip=$(hostname -I 2>/dev/null | awk '{print $1}')
    echo ""
    echo "============================================"
    echo " 惠福星链 · 部署完成"
    echo " Huifu StarChain · Deploy Complete"
    echo " 时间: $(date)"
    echo "============================================"
    echo ""
    echo " 访问地址:"
    echo "   前端:  http://${ip}"
    echo "   API:   http://${ip}/api/v1/health"
    echo "   MinIO: http://${ip}:9001"
    echo ""
    echo " 常用命令:"
    echo "   sudo systemctl status huifu-api"
    echo "   sudo journalctl -u huifu-api -f"
    echo "   sudo systemctl reload nginx"
    echo "   bash ${DEPLOY_DIR}/scripts/backup.sh"
    echo "   bash ${DEPLOY_DIR}/scripts/health-check.sh"
    echo ""
    echo " 首次登录: admin / admin123"
    echo " 日志目录: ${LOG_DIR}"
    echo "============================================"
}

# ============================================================
# Main
# ============================================================
main() {
    echo ""
    echo "============================================"
    echo " 惠福星链 · 一键自动部署（原生部署版）"
    echo " Huifu StarChain · Auto Deploy (Native)"
    echo " 时间: $(date)"
    echo " 参数: incremental=${INCREMENTAL} skip_build=${SKIP_BUILD} skip_db=${SKIP_DB}"
    echo "============================================"
    echo ""

    check_root
    detect_os
    load_env

    # ---- 非增量模式: 初始化目录、密钥、安装组件 ----
    if [ "$INCREMENTAL" = false ]; then
        setup_dirs_and_env
        load_env  # 重新加载刚生成的 .env

        # 安装组件（每个独立、非致命）
        install_java    || true
        install_maven   || true
        install_nodejs  || true
        install_mysql   || true
        install_redis   || true
        install_minio   || true
    fi

    # ---- 构建 ----
    if [ "$SKIP_BUILD" = false ]; then
        build_backend  || warn "后端构建失败"
        build_frontend || warn "前端构建失败"
    fi

    # ---- 部署配置 ----
    deploy_systemd_units

    # ---- 数据库初始化 ----
    if [ "$SKIP_DB" = false ]; then
        init_database
    fi

    # ---- 启动服务 ----
    start_services

    # ---- 维护 ----
    setup_cron
    health_check
    print_summary
}

main "$@"
