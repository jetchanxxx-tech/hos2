# language: zh-CN
# 惠福时光轴 · 全流程健康档案+随访 — BDD 功能规格
# 对应 PRD 模块一：惠福时光轴（全流程+健康档案+随访）

功能: 全周期时间轴
  作为一位星球居民
  我希望查看我/家庭成员从备孕到育儿的全周期健康时间轴
  以便一站式掌握全部诊疗记录

  背景:
    假定 已注册用户 "张芳"（userId=1001），已加入家庭 ID=100
    假定 张芳 登录并获取 accessToken

  # ============================================================
  # 场景 1：时间轴展示与过滤
  # ============================================================

  场景: 查看本人时间轴
    当 张芳 请求 GET /api/v1/records?userId=1001&page=1&size=20
    那么 返回 code=200，data 包含分页的 HealthRecord 列表
    并且 按 eventDate 倒序排列
    并且 每条记录含 eventTitle、eventDate、recordType、source、abnormalFlag

  场景: 按阶段筛选时间轴（孕期）
    当 张芳 请求 GET /api/v1/records?userId=1001&stage=GESTATION
    那么 返回孕期相关记录（gestationalWeek 非空 且 recordType 含 CHECKUP/REPORT/DELIVERY）
    并且 按 gestationalWeek 升序排列

  场景: 按来源筛选（仅看 HIS 数据）
    当 张芳 请求 GET /api/v1/records?userId=1001&source=HIS
    那么 仅返回 source=HIS 的记录

  场景: 按记录类型筛选
    当 张芳 请求 GET /api/v1/records?userId=1001&recordType=SELF_RECORD
    那么 仅返回居家自测记录

  # ============================================================
  # 场景 2：关键节点自动标注
  # ============================================================

  场景: 首次产检自动标注
    假定 张芳有一条 CHECKUP 记录，gestationalWeek="7W"
    当 查询时间轴
    那么 该记录显示关键节点标签 "首次产检"

  场景: NT 检查自动标注
    假定 张芳有一条 CHECKUP 记录，gestationalWeek="12W"，含 NT 值
    那么 该记录显示节点标签 "NT检查"

  场景: 分娩节点自动标注
    假定 张芳有一条 recordType=DELIVERY 的记录
    那么 该记录显示节点标签 "宝宝出生"，且作为时间轴分界点
    并且 分娩后自动创建育儿阶段起始标记

  场景: 里程碑高亮
    假定 张芳有 recordType=MILESTONE 的记录（如"宝宝满月""42天复查"）
    那么 该记录使用特殊图标（⭐）展示

  # ============================================================
  # 场景 3：报告汇聚
  # ============================================================

  场景: 查看某次记录的检验报告
    假定 存在健康记录 ID=500（OGTT 产检）
    当 张芳 请求 GET /api/v1/records/500/reports
    那么 返回该记录关联的 LabReport 列表
    并且 每条 LabReport 含 indicatorName、result、unit、referenceRange、isAbnormal

  场景: 异常指标标红
    假定 LabReport ID=50 的 isAbnormal=1（偏高），indicatorName="空腹血糖"
    当 查看报告列表
    那么 该异常指标行显示红色标记 "↑偏高"
    并且 有一个指向/异常/的tooltip展示可能的医学意义

  场景: 指标趋势图
    当 张芳 请求 GET /api/v1/records/trends/BG_FASTING?from=2026-01-01&to=2026-06-01
    那么 返回按日期排序的 LabReport 列表
    并且 前端使用 ECharts 渲染折线图（x=日期，y=指标值）+ 参考范围上下限虚线

  场景: 危急值预警
    假定 LabReport ID=51 的 isAbnormal=2（危急），indicatorName="血钾"
    当 该报告同步后
    那么 自动创建一条 FollowupTask，priority=URGENT
    并且 给对应医疗管家推送通知

  # ============================================================
  # 场景 4：居家记录
  # ============================================================

  场景: 手动录入血糖
    当 张芳 通过前端表单提交居家记录：recordType=SELF_RECORD，eventTitle="自测血糖"
       detailJson={"BG":{"value":6.8,"unit":"mmol/L","meal":"post","mealTime":120}}
    那么 创建 HealthRecord，source=MANUAL
    并且 返回 code=200

  场景: 录入体重并生成趋势
    假定 张芳有5条 recordType=SELF_RECORD 且 detailJson 含 weight 的记录
    当 查询 timeSeries?indicator=weight&from=2026-01-01&to=2026-06-01
    那么 返回 [(date, value), ...] 格式的时序数据
    并且 前端渲染体重趋势折线图

  场景: 设备同步心率
    当 系统收到来自蓝牙设备的心率记录（source=DEVICE，detailJson 含 heartRate）
    那么 自动创建 HealthRecord
    并且 source 设为 DEVICE
    并且 设备型号记录到 detailJson.deviceModel

  # ============================================================
  # 场景 5：智能随访
  # ============================================================

  场景: 按孕周自动生成随访任务
    假定 张芳当前 gestationalWeek="28W"（第三产程）
    假定 系统中存在孕期随访规则：28W 需进行妊娠糖尿病随访
    当 每日凌晨 3:00 执行规则引擎同步
    那么 自动为张芳创建一条 FollowupTask：taskType=GESTATIONAL，scheduledDate=今天+7天

  场景: 异常指标触发随访
    假定 张芳的 LabReport BG_FASTING > 5.1（妊娠糖尿病阈值）
    当 报告同步后触发规则引擎
    那么 自动创建 FollowupTask：taskType=ABNORMAL_LAB，priority=HIGH
    并且 分配至对应科室的 医疗管家

  场景: 随访逾期自动升级
    假定 FollowupTask ID=10，scheduledDate=3天前，status=PENDING
    当 每日凌晨 3:00 检测逾期任务
    那么 task ID=10 的 status 变为 OVERDUE
    并且 priority 升级为 URGENT
    并且 推送给主管

  场景: 管家完成随访
    假定 FollowupTask ID=10 已分配给 管家 "陈*华"（userId=2001）
    当 陈*华 PUT /api/v1/followups/10/complete，附带 completionNote 和 completionJson
    那么 status 变为 COMPLETED，completedAt 记录完成时间
    并且 在 AuditLog 中记录操作

  # ============================================================
  # 场景 6：AI 电话随访（P2 阶段）
  # ============================================================

  场景: 产后42天未复查自动外呼
    假定 张芳的分娩记录日期距今 42 天
    假定 分娩后无 recordType=CHECKUP 的记录
    当 每日上午 10:00 检测产后随访规则
    那么 创建一个 FollowupTask，followupMethod=AI_PHONE，taskType=POST_DISCHARGE
    并且 调用阿里云语音 API 发起外呼
    并且 aiCallId 记录通话 ID

  场景: AI 外呼结果回执
    假定 AI 外呼成功接通，时长 120 秒
    当 阿里云回执到达 webhook /api/v1/followups/ai-callback
    那么 更新 FollowupTask 的 completionJson 记录通话摘要
    并且 如果用户确认已复查，自动完成该任务
