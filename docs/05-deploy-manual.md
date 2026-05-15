# 惠福星链 · 部署手册

## 适用环境

- **操作系统**：CentOS 7.9+ / Ubuntu 20.04+ / Debian 11+
- **架构**：x86_64, 最低 4 核 8G 内存, 50G 可用磁盘
- **网络**：公网 80/443 端口开放，内网 3306/6379/9000 端口互通

---

## 1. 环境准备

### 1.1 安装基础依赖

```bash
# ---- Ubuntu/Debian ----
sudo apt update && sudo apt install -y \
    curl wget git openssl ca-certificates gnupg lsb-release

# ---- CentOS/RHEL ----
sudo yum install -y curl wget git openssl ca-certificates

# ---- 安装 Docker ----
curl -fsSL https://get.docker.com | sudo bash
sudo usermod -aG docker $USER
newgrp docker  # 或重新登录

# 验证安装
docker --version
docker-compose --version

# ---- 安装 Java 21 (后端构建使用) ----
# Ubuntu
sudo apt install -y openjdk-21-jdk

# CentOS
sudo yum install -y java-21-openjdk-devel

java -version  # 确认版本: 21.x

# ---- 安装 Node.js 20 (前端构建使用) ----
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

node --version  # 确认版本: 20.x
npm --version
```

### 1.2 创建目录结构

```bash
sudo mkdir -p /opt/huifu-starchain
sudo mkdir -p /backup/huifu-starchain
sudo mkdir -p /var/log/huifu-starchain
sudo chown -R $USER:$USER /opt/huifu-starchain
```

### 1.3 配置系统参数

```bash
# 配置内核参数 (/etc/sysctl.d/99-huifu.conf)
cat << 'EOF' | sudo tee /etc/sysctl.d/99-huifu.conf
net.core.somaxconn = 1024
net.ipv4.tcp_max_syn_backlog = 2048
vm.overcommit_memory = 1
vm.swappiness = 1
EOF

sudo sysctl -p /etc/sysctl.d/99-huifu.conf

# 配置文件描述符限制 (/etc/security/limits.d/huifu.conf)
cat << 'EOF' | sudo tee /etc/security/limits.d/huifu.conf
* soft nofile 65536
* hard nofile 65536
* soft nproc  4096
* hard nproc  4096
EOF
```

---

## 2. 部署应用

### 2.1 获取代码

```bash
cd /opt
git clone <repository-url> huifu-starchain
cd huifu-starchain
```

### 2.2 配置环境变量

```bash
cd deploy
cp .env.template .env
# 编辑 .env，填写数据库密码、JWT密钥等
vi .env
```

### 2.2.1 密钥生成

```bash
# 生成安全的随机密钥
echo "MYSQL_ROOT_PASSWORD: $(openssl rand -base64 24)"
echo "MYSQL_PASSWORD: $(openssl rand -base64 24)"
echo "REDIS_PASSWORD: $(openssl rand -base64 16)"
echo "JWT_SECRET: $(openssl rand -base64 48)"
echo "AES_KEY: $(openssl rand -base64 32)"

# 将以上输出填入 .env 文件对应字段
```

### 2.3 配置 SSL 证书

```bash
# 使用 Let's Encrypt（免费 SSL）
sudo apt install -y certbot
sudo certbot certonly --standalone -d huifu-starchain.your-domain.com

# 复制证书到 Nginx
sudo cp /etc/letsencrypt/live/huifu-starchain.your-domain.com/fullchain.pem deploy/nginx/ssl/huifu-starchain.crt
sudo cp /etc/letsencrypt/live/huifu-starchain.your-domain.com/privkey.pem deploy/nginx/ssl/huifu-starchain.key

# 设置自动续期
sudo crontab -e
# 0 3 * * * certbot renew --quiet && docker exec huifu-nginx nginx -s reload
```

### 2.4 构建和启动

```bash
# 完整部署（构建 + 启动）
cd /opt/huifu-starchain/deploy
chmod +x scripts/*.sh
bash scripts/deploy.sh prod

# 仅启动（已有镜像）
docker-compose up -d
```

### 2.5 验证部署

```bash
# 检查所有容器运行状态
docker-compose ps

# 预期输出：5 个服务均为 Up (healthy)
# NAME            STATUS
# huifu-nginx     Up (healthy)
# huifu-api       Up (healthy)
# huifu-mysql     Up (healthy)
# huifu-redis     Up (healthy)
# huifu-minio     Up (healthy)

# 测试 API
curl http://localhost/api/v1/health
# 预期: {"code":200,"message":"Huifu StarChain API is running"}

# 测试前端
curl -I http://localhost/
# 预期: HTTP/1.1 200 OK
```

---

## 3. 数据库初始化

### 3.1 确认 Flyway 迁移

```bash
# Flyway 在 API 启动时自动执行迁移
# 查看迁移状态
docker exec huifu-api ls -la /app/db/migration/

# 查看迁移日志
docker logs huifu-api | grep -i flyway
# 预期: "Successfully validated X migrations"
```

### 3.2 手动连接数据库

```bash
# 连接到 MySQL
docker exec -it huifu-mysql mysql -u huifu -p huifu_starchain

# 验证表结构
SHOW TABLES;
# 预期：看到 users, families, health_records 等 16 张表

# 验证种子数据
SELECT id, name_masked, role FROM users LIMIT 5;
SELECT id, name, type FROM service_packages WHERE status = 'ON_SHELF';
```

---

## 4. 首次系统配置

### 4.1 创建第一个管理员

```bash
# 通过 API 创建超级管理员（或修改种子用户）
curl -X POST http://localhost/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800000000",
    "password": "Admin@123456",
    "name": "系统管理员"
  }'

# 然后手动在数据库修改角色为 SUPER_ADMIN
docker exec -it huifu-mysql mysql -u huifu -p huifu_starchain \
  -e "UPDATE users SET role = 'SUPER_ADMIN' WHERE phone_hash = SHA2('13800000000', 256);"
```

### 4.2 配置服务包

登录管理后台 → 系统管理 → 服务包配置 → 创建服务包（或使用种子数据）

### 4.3 配置医院网关

修改 `.env` 文件：
```bash
HOSPITAL_GATEWAY_URL=http://10.0.0.100:8081
HOSPITAL_API_KEY=<医院提供的API密钥>
```

重启 API：
```bash
docker-compose restart api
```

---

## 5. SSL 配置（Nginx）

### 5.1 Let's Encrypt 自动配置

```bash
# 安装 certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d huifu-starchain.your-domain.com

# 设置自动续期
echo "0 3 * * * root certbot renew --quiet --post-hook 'docker exec huifu-nginx nginx -s reload'" | \
  sudo tee /etc/cron.d/certbot-renew
```

### 5.2 自签名证书（内网/测试环境）

```bash
mkdir -p deploy/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout deploy/nginx/ssl/huifu-starchain.key \
  -out deploy/nginx/ssl/huifu-starchain.crt \
  -subj "/CN=huifu-starchain.local"
```

---

## 6. 扩容与高可用

### 6.1 水平扩容 API

```bash
# docker-compose.yml 中修改 replicas
services:
  api:
    deploy:
      replicas: 3

# 或在 docker-compose 命令行中：
docker-compose up -d --scale api=3
```

### 6.2 数据库主从

```yaml
# 生产环境建议使用外部 MySQL 集群或云数据库
# 修改 .env 中 MYSQL_HOST 指向外部数据库
```

---

## 7. 卸载

```bash
# ⚠️ 高危操作：完全删除所有数据
cd /opt/huifu-starchain/deploy
docker-compose down -v  # 删除容器和卷
sudo rm -rf /opt/huifu-starchain
sudo rm -rf /backup/huifu-starchain
sudo rm -rf /var/log/huifu-starchain
```

---

## 8. 故障排除

| 问题 | 解决方案 |
|------|----------|
| 端口被占用 | `lsof -i :80` → kill 占用进程 |
| 数据库连接拒绝 | 检查 MYSQL_HOST、端口、密码 |
| Flyway 迁移失败 | 检查 SQL 文件语法 → 修复后重启 API |
| 容器无法启动 | `docker logs huifu-api` 查看错误日志 |
| 磁盘空间不足 | `docker system prune -a` 清理 |

---

## 9. 附录：完整部署检查清单

- [ ] 操作系统依赖已安装（Docker, Java 21, Node.js 20）
- [ ] 目录已创建（/opt/huifu-starchain, /backup, /var/log）
- [ ] .env 文件已配置（所有密钥已替换为随机值）
- [ ] SSL 证书已配置
- [ ] docker-compose 启动成功（5个服务）
- [ ] API 健康检查通过（/api/v1/health）
- [ ] 数据库迁移完成（Flyway 日志确认）
- [ ] 种子数据已导入
- [ ] 前端页面可访问
- [ ] 登录功能正常
- [ ] 备份脚本已配置 crontab
- [ ] 监控告警已配置
- [ ] 文档已归档
