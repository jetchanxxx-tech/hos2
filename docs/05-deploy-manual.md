# 惠福星链 · 部署手册（原生部署版）

## 适用环境

- **操作系统**：CentOS 7.9+ / Rocky 8+ / Ubuntu 20.04+ / Debian 11+
- **架构**：x86_64, 最低 4 核 8G 内存, 50G 可用磁盘
- **网络**：公网 80/443 端口开放，内网 3306/6379/9000 端口互通

---

## 1. 快速部署

### 1.1 一键部署（推荐）

```bash
cd /opt
git clone <repository-url> huifu-starchain
cd huifu-starchain/deploy
sudo bash auto-deploy.sh
```

脚本自动完成：OS 检测 → 安装缺失组件 → 构建前后端 → 初始化数据库 → 启动服务 → 配置备份。

### 1.2 命令参数

| 参数 | 用途 |
|------|------|
| (无) | 完整部署：检测、安装、构建、部署 |
| `--incremental` | 跳过组件安装，仅重建+重启 |
| `--skip-build` | 跳过构建，使用已有产物重启 |
| `--skip-db` | 跳过数据库初始化 |

---

## 2. 手动部署

### 2.1 安装依赖

```bash
# ---- Ubuntu/Debian ----
sudo apt update && sudo apt install -y openjdk-21-jdk maven nginx mysql-server-8.0 redis-server curl wget git openssl
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# ---- CentOS/RHEL ----
sudo dnf install -y epel-release
sudo dnf install -y java-21-openjdk-devel maven nginx mysql-server redis curl wget git openssl
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo dnf install -y nodejs

# MinIO（所有发行版通用，下载二进制）
sudo curl -fsSL -o /usr/local/bin/minio https://dl.min.io/server/minio/release/linux-amd64/minio
sudo chmod +x /usr/local/bin/minio
```

### 2.2 创建目录和用户

```bash
sudo mkdir -p /opt/huifu-starchain /backup/huifu-starchain /var/log/huifu-starchain
sudo useradd -r -s /sbin/nologin -d /opt/huifu-starchain huifu
```

### 2.3 配置环境变量

```bash
cd /opt/huifu-starchain/deploy
cp .env.template .env
# 编辑 .env，填写密钥和远程服务地址
vi .env
```

**服务主机配置**：`MYSQL_HOST`/`REDIS_HOST`/`MINIO_HOST` 默认为 `localhost`。设为非 localhost 值时，部署脚本将跳过对应组件的本地安装和管理。

### 2.4 构建

```bash
# 后端
cd /opt/huifu-starchain/backend
mvn clean package -DskipTests
ln -sf target/starchain-1.0.0-SNAPSHOT.jar target/starchain-1.0.0.jar

# 前端
cd /opt/huifu-starchain/frontend
npm ci
npm run build
```

### 2.5 部署配置

```bash
# Nginx 配置
sudo cp /opt/huifu-starchain/deploy/nginx/nginx.conf /etc/nginx/nginx.conf
sudo nginx -t && sudo systemctl reload nginx

# Systemd 配置
sudo cp /opt/huifu-starchain/deploy/systemd/huifu-api.service /etc/systemd/system/
sudo systemctl daemon-reload
```

### 2.6 初始化数据库

```bash
# 启动 MySQL
sudo systemctl enable --now mysql

# 创建数据库和用户
sudo mysql -e "CREATE DATABASE IF NOT EXISTS huifu_starchain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER IF NOT EXISTS 'huifu'@'localhost' IDENTIFIED BY '<密码>';"
sudo mysql -e "GRANT ALL PRIVILEGES ON huifu_starchain.* TO 'huifu'@'localhost'; FLUSH PRIVILEGES;"
```

> Flyway 迁移由 API 启动时自动执行，无需手动运行 SQL。

### 2.7 启动服务

```bash
sudo systemctl enable --now mysql redis minio nginx huifu-api
```

**启动顺序**：MySQL → Redis → MinIO → Nginx → huifu-api（已由 systemd 依赖关系自动处理）

### 2.8 验证部署

```bash
# 检查服务状态
sudo systemctl status huifu-api nginx mysql redis minio

# 测试 API
curl http://localhost:8080/api/v1/health

# 测试 Nginx → API 代理
curl http://localhost/api/v1/health

# 前端页面
curl -I http://localhost/
```

---

## 3. MinIO 配置

### 3.1 安装 MinIO systemd 服务

auto-deploy.sh 自动完成。手动步骤：

```bash
sudo useradd -r -s /sbin/nologin -d /var/lib/minio minio
sudo mkdir -p /var/lib/minio/data /etc/minio

cat << 'EOF' | sudo tee /etc/minio/minio.conf
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=<密码>
EOF

cat << 'EOF' | sudo tee /etc/systemd/system/minio.service
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
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now minio
```

### 3.2 访问 MinIO 控制台

浏览器打开 `http://<服务器IP>:9001`，使用 `.env` 中 `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` 登录。

---

## 4. 数据库操作

### 4.1 连接数据库

```bash
sudo mysql -u huifu -p huifu_starchain
```

### 4.2 验证表结构

```sql
SHOW TABLES;
-- 预期：16 张表（users, families, health_records, service_packages 等）
```

### 4.3 验证种子数据

```sql
SELECT id, name_masked, role FROM users LIMIT 5;
SELECT id, name, type FROM service_packages WHERE status = 'ON_SHELF';
```

---

## 5. SSL 配置

### 5.1 Let's Encrypt（推荐）

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d huifu-starchain.your-domain.com

# 自动续期
echo "0 3 * * * root certbot renew --quiet --post-hook 'systemctl reload nginx'" | \
  sudo tee /etc/cron.d/certbot-renew
```

### 5.2 自签名证书（内网/测试）

```bash
sudo mkdir -p /opt/huifu-starchain/deploy/nginx/ssl
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.key \
  -out /opt/huifu-starchain/deploy/nginx/ssl/huifu-starchain.crt \
  -subj "/CN=huifu-starchain.local"
```

---

## 6. 日常运维

### 6.1 常用命令

```bash
# 查看 API 日志
sudo journalctl -u huifu-api -f

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/access.log

# 查看应用日志
sudo tail -f /var/log/huifu-starchain/application.log

# 重启服务
sudo systemctl restart huifu-api
sudo systemctl reload nginx

# 运行健康检查
sudo bash /opt/huifu-starchain/deploy/scripts/health-check.sh
```

### 6.2 备份

```bash
# 手动备份
sudo bash /opt/huifu-starchain/deploy/scripts/backup.sh

# 查看 crontab 是否已配置
crontab -l | grep backup
# 预期: 0 2 * * * bash /opt/huifu-starchain/deploy/scripts/backup.sh ...
```

### 6.3 更新部署

```bash
cd /opt/huifu-starchain
git pull

# 增量部署（仅重建+重启）
sudo bash deploy/auto-deploy.sh --incremental
```

---

## 7. 故障排除

| 问题 | 解决方案 |
|------|----------|
| 端口 80 被占用 | `sudo lsof -i :80` → 停用占用进程或改 Nginx 端口 |
| 端口 8080 被占用 | `sudo lsof -i :8080` → 检查是否有旧 API 进程残留 |
| MySQL 无法连接 | `sudo systemctl status mysql`，检查 `/var/log/mysql/error.log` |
| Flyway 迁移失败 | `sudo journalctl -u huifu-api \| grep -i flyway`，检查 SQL 冲突 |
| API 启动超时 | `sudo journalctl -u huifu-api -n 50`，检查 Java/Memory/数据库连接 |
| Nginx 502 | 确认 API 是否启动 (`systemctl status huifu-api`)，检查 `nginx -t` |
| SELinux 拦截 | `sudo setsebool -P httpd_can_network_connect 1`，`sudo chcon -R -t httpd_sys_content_t /opt/huifu-starchain/frontend/dist` |
| 磁盘空间不足 | `sudo journalctl --vacuum-size=500M`，清理旧备份 |

---

## 8. 卸载

```bash
# ⚠️ 高危操作：完全删除所有数据和配置

# 停用服务
sudo systemctl disable --now huifu-api minio

# 删除文件
sudo rm -rf /opt/huifu-starchain
sudo rm -rf /backup/huifu-starchain
sudo rm -rf /var/log/huifu-starchain

# 删除 systemd 配置
sudo rm -f /etc/systemd/system/huifu-api.service /etc/systemd/system/minio.service
sudo systemctl daemon-reload

# MySQL 数据库（如需清理）
# sudo mysql -e "DROP DATABASE huifu_starchain; DROP USER 'huifu'@'localhost';"
```

---

## 9. 部署检查清单

- [ ] 操作系统依赖已安装（Java 21, Maven, Node.js 20, Nginx, MySQL, Redis, MinIO）
- [ ] 目录已创建（/opt/huifu-starchain, /backup, /var/log）
- [ ] .env 已配置（密钥已替换，服务主机地址正确）
- [ ] 前后端构建成功（JAR + dist/）
- [ ] Nginx 配置已部署（nginx -t 通过）
- [ ] Systemd unit 已安装
- [ ] MySQL 数据库已初始化（Flyway 迁移完成）
- [ ] 所有 5 个服务运行中（systemctl status）
- [ ] API 健康检查通过（/api/v1/health）
- [ ] Nginx→API 代理正常（curl localhost/api/v1/health）
- [ ] 前端页面可访问
- [ ] 登录功能正常（admin / admin123）
- [ ] 备份脚本已配置 crontab
- [ ] SSL 证书已配置（生产环境）
