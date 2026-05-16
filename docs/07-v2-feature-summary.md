# 惠福星链 V1.0 → V2.0 功能总结

## 版本概览

| 版本 | 定位 | 状态 |
|------|------|------|
| V1.0 | MVP：四大模块骨架 + 自动化部署 | 已完成 |
| V2.0 | 增强：演示数据 + IM对接 + 同心圆交互 + 管理后台细化 | 已完成 |

## 一、全局基础设施

### 1.1 部署架构（Docker → Systemd 原生部署）
- **舍弃 Docker**，前后端本地 Maven/npm 构建
- **auto-deploy.sh**：OS 检测（CentOS/Ubuntu/Debian）→ 组件安装（Java 21/Maven/Node.js/MySQL/Redis/MinIO）→ 构建 → systemd 部署
- **MySQL 5.7 兼容**：自动检测版本 → `mysql_native_password` 认证 → `characterEncoding=UTF-8` → `ddl-auto=none`
- **Redis 密码处理**：空密码 `REDIS_PASSWORD=` 会导致 Redisson 发送 AUTH 失败 → 强制 `nishi250`
- **init_database**：同时创建 `'huifu'@'127.0.0.1'` 和 `'huifu'@'localhost'`（JDBC TCP 连接）
- **Nginx 解耦**：部署脚本不检测/安装 Nginx，仅负责复制配置和 reload

### 1.2 测试基础设施
- 26 个自动化 JUnit 测试，H2 内存库运行，禁用 Redis 和 MinIO
- `ConcentricCircleTest` — 12 场景
- `ApiSmokeTest` — 14 场景（注册/登录/同心圆/灵犀/星盘/时光轴/随访）

### 1.3 数据库迁移
| 版本 | 内容 |
|------|------|
| V1 | 16 张核心表 schema |
| V2 | 种子测试数据（管理员+管家+服务包） |
| V3 | 全方位演示数据（用户 13800001111，含 18 条健康记录、11 份检验报告、5 个随访任务、2 段聊天会话等） |
| cleanup | 生产环境数据清理脚本（MySQL 5.7 兼容） |

## 二、惠福同心圆（家庭健康账户）

### 2.1 已实现功能
- 家庭创建/解散
- 成员添加（搜索用户）/删除
- 共享权限：ALL / REPORT_ONLY / BASIC_ONLY / NONE（强制 enforcement）
- 紧急联系人切换
- **邀请码加入已有家庭**（V2.0 新增）：输入 HF 开头邀请码 → 自动加入
- **邀请码注册闭环**：注册时输入邀请码 → 自动加入对应家庭（relationship=OTHER）
- 邀请码分享（复制到剪贴板）

### 2.2 V2.0 新增
- 邀请码加入家庭输入框（空状态下显示"加入已有家庭"按钮）
- HealthRecordService 共享权限 enforcement：resolveShareScope → maskByScope
- 前端完整操作界面（弹窗搜索添加、下拉编辑共享范围、checkbox 紧急联系人）

### 2.3 待实现（P2）
- 跨科转介功能
- 服务包权益家庭成员共享扣减

## 三、惠福时光轴（全流程+健康档案+随访）

### 3.1 已实现功能
- 全周期时间轴（按用户 + 类型筛选 + 分页）
- 报告汇聚（按日期范围查询）
- LabReport 指标趋势图
- 异常指标标红 + 单独查询
- 数据来源统计（HIS/LIS/MANUAL）
- 居家自录（SELF_RECORD + MANUAL 类型）

### 3.2 V2.0 新增
- HealthRecord 补全 getter/setter（source、abnormalFlag、alertTriggered、isDeleted）
- V3 演示数据：18 条完整产检链（备孕→确认妊娠→NT→糖耐量异常→复查→大排畸→分娩→产后42天→宝宝3月体检→盆底康复→自测血压）
- 异常指标（OGTT_1H=10.2 标红、产后 HGB=105 标红）

### 3.3 待实现（P2）
- ECharts 趋势图前端对接
- 关键节点自动标注（NT/糖耐/分娩等）
- 智能随访规则引擎（自动生成任务）

## 四、惠福灵犀（智能客服+随访）

### 4.1 已实现功能
- 三层 FAQ 知识库（MySQL FULLTEXT 索引）
- **紧急关键词检测**（出血/剧痛/破水/晕倒/抽搐/呼吸困难/大出血）→ triggerAlert + escalationLevel=URGENT
- **意图识别**（V2.0 新增）：MEDICAL/BENEFIT/COMPLAINT/APPOINTMENT/GENERAL 五类，基于关键词规则
- **情绪检测**（V2.0 新增）：ANGRY/ANXIOUS/POSITIVE/NEUTRAL 四类，基于关键词规则
- **IM 适配器接口**（V2.0 新增）：统一 ImAdapter 接口，支持 InternalChatAdapter / WechatImAdapter 等实现
- 会话管理（create/list）
- 消息列表（按 seqNo 排序）
- AI 自动回复（FAQ 匹配 → 非紧急时自动返回）
- 预警列表 API（getAlerts）
- 升级标记（escalationLevel）

### 4.2 V2.0 新增
- `ImAdapter` 接口 + `InternalChatAdapter`（内置聊天）+ `WechatImAdapter`（预留微信对接）
- ChatService.sendMessage 自动写入 intentType/sentimentLabel
- **IM 回调接口**（V2.0）：POST /api/v1/chat/callback/{channel} → 接收微信/QQ/飞书推送消息
- V3 演示数据：2 段完整客服对话（包含 AI 秒回 + 管家人工回复）
- 管理后台新增"客服会话"Tab：查看所有会话（意图/状态/升级级别）

### 4.3 IM 对接架构
```
用户端（小程序/微信/QQ/飞书）
        ↕ ImAdapter 接口
ChatController → ChatService → ChatSession / ChatMessage / KnowledgeArticle
        ↕
InternalChatAdapter（内置） / WechatImAdapter（预留） / FeishuImAdapter（待实现）
```

### 4.4 待实现（P2）
- 大模型多轮对话集成（OpenAI/DeepSeek API）
- 10 分钟紧急回电定时器
- 意图识别升级为 LLM 分类
- 情绪分析升级为 LLM 打分

## 五、惠福星盘（运营驾驶舱）

### 5.1 已实现
- KPI 摘要（活跃会员/本月新增/家庭数/月营收/随访完成/满意度/随访率）
- 服务包销售排行（销量+营收+价格+状态）
- 最近动态（审计日志翻译为可读活动流）
- 医护积分榜（随访完成数 + 积分计算）

### 5.2 V2.0 新增
- DashboardService 去 Mock 化：满意度从 ChatSession 真实聚合、积分从随访统计数据
- 新增端点：satisfaction-distribution / followup-by-butler / conversion-funnel / revenue-trend
- 转化漏斗：注册→绑家庭→购买→转化率（真实 DB 查询）
- 营收趋势：近 12 个月逐月收入对比
- AuditLog → 活动流格式化（actor / action / target / time）
- 新增 Dashboard KPI：packageOrderCount

### 5.3 待实现（P2）
- 转化漏斗
- 财务对账
- 满意度趋势图
- 投诉闭环率
- 医护积分引擎（多维度积分计算）

## 六、系统管理后台

### 6.1 已实现功能
- 用户管理：列表/搜索/角色切换/启停
- 服务包管理：CRUD + 上下架
- 审计日志：分页查看/按用户筛选
- 同步监控：医院网关同步状态
- 知识库管理：FAQ 创建/编辑
- 管理统计（用户总数/角色分布）

### 6.2 V2.0 新增
- AdminDashboard.vue（4 Tab：用户/服务包/知识库/审计日志）
- AdminController 知识库 CRUD（list/create/update）
- userApi.updateProfile（个人资料编辑页面）
- 前端 ProfileEdit.vue（修改姓名/性别/出生日期/头像）

### 6.3 待实现（P2）
- 操作日志查看前端页面
- 服务包订单管理页面
- 接口监控仪表盘
- RBAC 权限拦截器（SecurityConfig 角色检查细化）

## 七、个人中心

### 7.1 V2.0 新增
- ProfileEdit.vue：个人资料编辑页（姓名/性别/出生日期/头像 URL）
- PUT /api/v1/users/me/profile 端点
- 路由 /profile

## 八、前端架构

### 8.1 页面路由
| 路径 | 模块 | 组件 |
|------|------|------|
| /dashboard | 星盘 | OperationalDashboard |
| /dashboard/members | 星盘 | MemberAnalytics |
| /timeline | 时光轴 | TimelineView |
| /lingxi/chat | 灵犀 | ChatDashboard |
| /lingxi/followup | 灵犀 | FollowupManager |
| /concentric | 同心圆 | FamilyOverview |
| /profile | 个人 | ProfileEdit (V2.0) |
| /system/users | 管理 | UserManagement |
| /system/packages | 管理 | PackageManager |
| /system/admin | 管理 | AdminDashboard (V2.0) |

### 8.2 组件库
- KpiCard（数值卡片 + 趋势箭头）
- DataTable（泛型表格，支持 column 自定义 + slot）
- StatusPill（状态标签：active/blocked/pending/draft）

## 九、数据安全与合规

- 手机号 SHA-256 Hash → 加密存储
- 姓名脱敏：`张三丰` → `张*丰`
- JWT 双 Token 模式（accessToken 2h + refreshToken 7d）
- API 鉴权：JwtAuthFilter + @AuthenticationPrincipal
- 审计日志（traceId/userId/action/resourceType/ipAddress）
- 等保三级预设计（TLS 1.2+、用户授权、数据脱敏）

## 十、部署清单

| 环境 | 组件 | 版本要求 |
|------|------|----------|
| 服务器 | Linux（CentOS 7+/Ubuntu 20+/Debian 11+） | x86_64 |
| Java | JDK | 21 |
| 数据库 | MySQL | 5.7 或 8.0 |
| 缓存 | Redis | 5+ |
| 对象存储 | MinIO | latest |
| Web 服务器 | Nginx | 1.18+ |
| Node.js | 仅前端构建 | 18+ |
| Maven | 后端构建 | 3.6+ |

### 一键部署命令
```bash
cd /opt/huifu-starchain/deploy
sudo bash auto-deploy.sh          # 完整部署
sudo bash auto-deploy.sh --incremental  # 增量更新
```

### 演示数据库导入
```bash
mysql -u root -p huifu_starchain < database/migrations/V3__demo_data.sql
```

### 演示账号
| 角色 | 手机号 | 密码 |
|------|--------|------|
| 居民 | 13800001111 | test123 |
| 管理员 | 注册后手动设置角色 | — |
