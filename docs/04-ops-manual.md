# 惠福星链 · 运维手册

## 1. 系统概览

### 1.1 服务清单

| 服务 | 端口 | 用途 |
|------|------|------|
| Nginx | 80, 443 | 反向代理 + 静态资源 |
| Spring Boot API | 8080 | 后端业务 API |
| MySQL | 3306 | 主数据库 |
| Redis | 6379 | 缓存 + 会话 |
| MinIO | 9000, 9001 | 对象存储 (API + Console) |

### 1.2 关键路径

| 路径 | 说明 |
|------|------|
| `/opt/huifu-starchain` | 应用根目录 |
| `/var/log/huifu-starchain` | API 日志 |
| `/var/log/nginx` | Nginx 日志 |
| `/backup/huifu-starchain` | 数据库备份 |
| `/var/lib/docker/volumes` | Docker 数据卷 |

---

## 2. 日常运维

### 2.1 服务管理

```bash
# 启动所有服务
cd /opt/huifu-starchain/deploy && docker-compose up -d

# 停止所有服务
docker-compose down

# 重启单个服务
docker-compose restart api
docker-compose restart nginx

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f --tail=100 api
docker-compose logs -f --tail=100 mysql
```

### 2.2 健康检查

```bash
# 运行健康检查脚本
/opt/huifu-starchain/deploy/scripts/health-check.sh

# 手动检查 API
curl -f http://localhost/api/v1/health
# 预期返回: {"code":200,"message":"Huifu StarChain API is running"}

# 检查数据库
docker exec huifu-mysql mysqladmin ping -u root -p
```

### 2.3 数据库备份

```bash
# 手动备份
/opt/huifu-starchain/deploy/scripts/backup.sh

# 自动备份（crontab）
# 每日凌晨 2:00 执行
0 2 * * * /opt/huifu-starchain/deploy/scripts/backup.sh >> /var/log/huifu-backup.log 2>&1
```

### 2.4 日志轮转

```bash
# Nginx 日志轮转 (/etc/logrotate.d/nginx)
/var/log/nginx/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    postrotate
        docker exec huifu-nginx nginx -s reopen
    endscript
}

# API 日志轮转 (/etc/logrotate.d/huifu-api)
/var/log/huifu-starchain/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    copytruncate
}
```

---

## 3. 监控与告警

### 3.1 监控指标

| 指标 | 阈值 | 告警动作 |
|------|------|----------|
| API 响应时间 P95 > 2s | 持续 5min | 检查数据库连接池、GC |
| 磁盘使用率 > 85% | 即时 | 清理日志/扩容 |
| MySQL 连接数 > 15 | 持续 5min | 检查慢查询 |
| 容器异常退出 | 即时 | 重启容器 |
| 随访完成率 < 85% | 日终 | 管理员通知 |

### 3.2 Prometheus 指标

```bash
# 暴露的指标端点
curl http://localhost:8080/actuator/prometheus

# 关键指标
jvm_memory_used_bytes
http_server_requests_seconds_count
hikaricp_connections_active
```

### 3.3 应急响应

```bash
# API 服务不响应
docker-compose restart api

# 数据库连接异常
docker exec huifu-mysql mysql -u root -p -e "SHOW PROCESSLIST;"
docker-compose restart mysql

# Redis 异常
docker exec huifu-redis redis-cli -a $REDIS_PASSWORD PING
docker-compose restart redis

# 磁盘空间不足
du -sh /var/lib/docker/volumes/*
docker system prune -a --volumes  # ⚠️ 高危操作，确认后执行
```

---

## 4. 故障处理

### 4.1 常见故障

| 故障现象 | 可能原因 | 处理步骤 |
|----------|----------|----------|
| API 返回 502 | API 容器未启动 | `docker-compose ps api` → `docker-compose restart api` |
| 登录失败 | Redis/JWT 问题 | 检查 Redis 连接 → 重启 Redis |
| 数据库写入慢 | 连接池耗尽 | 检查 SHOW PROCESSLIST → kill 慢查询 |
| 文件上传失败 | MinIO 异常 | 检查 MinIO 状态 → 重启 MinIO |
| 前端白屏 | Nginx 配置错误 | 检查 nginx.conf → `docker exec huifu-nginx nginx -t` |

### 4.2 紧急回滚

```bash
# 回滚到上一个版本
cd /opt/huifu-starchain/deploy
docker-compose down
export APP_VERSION=1.0.0-previous
docker-compose up -d

# 回滚数据库
docker exec huifu-mysql mysql -u root -p huifu_starchain < /backup/huifu-starchain/mysql_上一次正常时间.sql
```

---

## 5. 性能调优

### 5.1 MySQL 调优

```ini
# /etc/mysql/conf.d/huifu.cnf
[mysqld]
innodb_buffer_pool_size = 512M
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
max_connections = 200
slow_query_log = 1
long_query_time = 1
```

### 5.2 JVM 调优

```bash
# Dockerfile ENTRYPOINT
-Xms512m -Xmx2g           # 堆内存
-XX:+UseZGC               # ZGC 低延迟 GC
-XX:ZCollectionInterval=120  # GC 间隔
```

---

## 6. 安全检查清单

- [ ] TLS 证书有效期检查（每月）
- [ ] 数据库备份恢复演练（每季度）
- [ ] 审计日志完整性检查（每季度）
- [ ] 渗透测试（每半年）
- [ ] 权限矩阵审查（人员变动时）
- [ ] 第三方依赖漏洞扫描（每周）

---

## 7. 联系信息

| 角色 | 联系方式 |
|------|----------|
| 荔小福技术值班 | dev@huifustarchain.com |
| 紧急响应热线 | 400-XXX-XXXX |
| 医院信息科 | 按各院实际配置 |
