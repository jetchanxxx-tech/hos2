# language: zh-CN
# 系统管理 · 角色权限+服务包配置+审计日志 — BDD 功能规格
# 对应 PRD 系统管理部分

功能: 用户与角色管理
  作为一位超级管理员
  我希望管理所有用户的角色和状态
  以便维护平台的权限体系

  背景:
    假定 超级管理员 "系统管理员"（userId=9999，role=SUPER_ADMIN）
    假定 系统管理员 登录并获取 accessToken

  # ============================================================
  # 场景 1：用户管理 CRUD
  # ============================================================

  场景: 查看用户列表
    当 系统管理员 请求 GET /api/v1/admin/users?page=1&size=20
    那么 返回分页用户列表，每项含 id、nameMasked、role、status、phone(脱敏)、createdAt
    并且 支持按 role 筛选

  场景: 修改用户角色
    当 系统管理员 PUT /api/v1/admin/users/{id}/role?role=BUTLER_MEDICAL
    那么 该用户 role 更新为 BUTLER_MEDICAL
    并且 AuditLog 记录操作

  场景: 启停用户账号
    当 系统管理员 PUT /api/v1/admin/users/{id}/status?status=SUSPENDED
    那么 该用户 status 变为 SUSPENDED
    并且 该用户无法登录（返回 40103 账号已被停用）

  场景: 搜索用户
    当 系统管理员 请求 GET /api/v1/admin/users?keyword=138
    那么 返回手机号包含 138 的用户列表

  场景: 查看用户详情
    当 系统管理员 请求 GET /api/v1/admin/users/{id}
    那么 返回完整用户信息（含 familyId、注册时间、最后登录时间、数据授权状态）

  # ============================================================
  # 场景 2：服务包管理
  # ============================================================

  场景: 创建新服务包
    当 OPS_ADMIN POST /api/v1/admin/packages
       {name:"VIP 家庭套", type:"VIP", price:22800, durationDays:365, benefitsJson:"...", category:"MATERNITY"}
    那么 创建 ServicePackage，status=DRAFT
    并且 返回 code=200 及 ServicePackage 对象

  场景: 服务包上架
    假定 存在 DRAFT 状态的服务包 ID=301
    当 OPS_ADMIN PUT /api/v1/admin/packages/301?status=ON_SHELF
    那么 status 变为 ON_SHELF
    并且 用户端可看到该服务包

  场景: 服务包下架
    当 OPS_ADMIN PUT /api/v1/admin/packages/301?status=OFF_SHELF
    那么 status 变为 OFF_SHELF
    并且 用户端不再显示该服务包
    并且 已有订单不受影响

  场景: 编辑服务包详情
    当 OPS_ADMIN PUT /api/v1/admin/packages/301，修改 price 和 benefitsJson
    那么 返回更新后的 ServicePackage

  # ============================================================
  # 场景 3：审计日志
  # ============================================================

  场景: 查看审计日志
    当 系统管理员 请求 GET /api/v1/admin/audit-logs?page=1&size=50
    那么 返回分页审计日志，每项含：
      | traceId | 追踪 ID |
      | userId | 操作人 ID |
      | userName | 操作人姓名 |
      | action | 操作类型 |
      | resourceType | 资源类型 |
      | resourceId | 资源 ID |
      | ipAddress | 来源 IP |
      | createdAt | 操作时间 |
      | result | SUCCESS/FAIL |

  场景: 按用户筛选审计日志
    当 系统管理员 请求 GET /api/v1/admin/audit-logs?userId=1001
    那么 仅返回 userId=1001 的操作记录

  场景: 审计日志自动记录
    当 任何用户执行敏感的写操作（创建/修改/删除）
    那么 系统自动写入 AuditLog
    并且 包含 traceId（可关联分布式追踪）

  # ============================================================
  # 场景 4：接口监控（医院网关同步）
  # ============================================================

  场景: 查看同步状态
    当 系统管理员 请求 GET /api/v1/admin/sync-status
    那么 返回同步状态列表，含 syncType、lastSyncAt、status（SUCCESS/FAIL/PENDING）、retryCount、errorMsg

  场景: 手动触发重试
    当 系统管理员 POST /api/v1/admin/sync-status/{id}/retry
    那么 重置 retryCount，重新发起同步请求

  # ============================================================
  # 场景 5：知识库管理
  # ============================================================

  场景: 管理员新增 FAQ
    当 OPS_ADMIN POST /api/v1/admin/knowledge
       {question:"NT检查正常范围是多少", answer:"正常范围 < 3.0mm", category:"PRENATAL", tags:"NT,产检,B超"}
    那么 创建 KnowledgeArticle，status=PUBLISHED
    并且 前端知识搜索可命中

  场景: 管理员下架 FAQ
    当 OPS_ADMIN PUT /api/v1/admin/knowledge/{id}?status=ARCHIVED
    那么 该 FAQ 不再出现在搜索结果中
