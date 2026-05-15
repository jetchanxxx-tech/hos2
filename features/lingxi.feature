# language: zh-CN
# 惠福灵犀 · AI+HCRM 智能客服 — BDD 功能规格
# 对应 PRD 模块三：惠福灵犀（AI+HCRM）

功能: AI 智能问答与客服对话
  作为一位星球居民或管家
  我希望通过聊天窗口咨询健康问题并获得智能回复
  以便快速解决疑问，必要时获得人工支持

  背景:
    假定 已注册用户 "张芳"（userId=1001）
    假定 医疗管家 "陈*华"（userId=2001，role=BUTLER_MEDICAL）
    假定 知识库中有 FAQ 文章 "孕期饮食注意事项"（category=NUTRITION，status=PUBLISHED）
    假定 张芳 登录并获取 accessToken

  # ============================================================
  # 场景 1：FAQ 智能问答（第一层 AI）
  # ============================================================

  场景: 用户提问命中 FAQ
    当 张芳 在聊天中发送消息 "孕期能喝咖啡吗"
    那么 系统搜索 knowledge_articles 全文本
    并且 匹配到 "孕期饮食注意事项"
    并且 自动回复该 FAQ 的 answer 内容
    并且 消息的 aiAnswerSource 为 "FAQ"

  场景: 用户提问未命中 FAQ
    当 张芳 发送消息 "我的手指最近发麻"
    那么 系统搜索 knowledge_articles 未找到匹配
    并且 自动回复 "您的问题已转接人工管家，请稍候"
    并且 会话 status 变为 WAITING_BUTLER

  场景: 多轮对话上下文保持
    假定 张芳 刚问过 "孕期能喝咖啡吗"
    当 张芳 追问 "那每天最多喝多少"
    那么 系统识别这是对上一个问题的追问
    并且 从 FAQ 中提取对应的每日限量信息回复

  # ============================================================
  # 场景 2：意图识别与自动分流（第二层 AI）
  # ============================================================

  场景: 医疗问题分流到医疗管家
    当 张芳 发送消息 "我有点腹痛"
    那么 系统识别 intentType=MEDICAL，confidence > 0.7
    并且 将该会话推送到 BUTLER_MEDICAL 的工作台

  场景: 权益问题分流到服务管家
    当 张芳 发送消息 "我想用陪诊券"
    那么 系统识别 intentType=BENEFIT，confidence > 0.7
    并且 将该会话推送到 BUTLER_SERVICE 的工作台

  场景: 投诉分流到主管
    当 张芳 发送消息 "我要投诉，你们服务太差了"
    那么 系统识别 intentType=COMPLAINT，confidence > 0.7
    并且 escalationLevel 自动设为 URGENT
    并且 会话推送到 OPS_ADMIN 角色

  场景: 意图置信度低时默认人工
    当 张芳 发送消息 "我吃了一个苹果"
    那么 系统识别 intentType=GENERAL，confidence < 0.5
    并且 不自动分流，保持 FAQ 回答模式

  # ============================================================
  # 场景 3：紧急预警
  # ============================================================

  场景: 触发紧急关键词
    当 张芳 发送消息 "我突然出血了"
    那么 关键词 "出血" 命中 containsEmergencyKeyword
    并且 消息 triggerAlert=true，alertKeyword="出血"
    并且 会话 escalationLevel=URGENT，status=WAITING_BUTLER
    并且 前端显示 🚨 紧急标记，管家端弹窗提醒

  场景: 紧急会话10分钟未处理升级
    假定 会话 ID=300，status=WAITING_BUTLER，escalationLevel=URGENT
    并且 最后一条紧急消息发送于 10 分钟前
    当 系统每 1 分钟扫描一次待处理紧急会话
    那么 会话的 escalationLevel 变为 CRITICAL
    并且 推送通知到 OPS_ADMIN

  场景: 非紧急消息不触发预警
    当 张芳 发送消息 "今天天气不错"
    那么 triggerAlert=false
    并且 会话状态保持 ACTIVE

  # ============================================================
  # 场景 4：情绪识别与升级
  # ============================================================

  场景: 识别焦虑情绪
    当 张芳 连续发送 3 条消息 "医生在吗""有人吗""我好担心"
    那么 系统分析 sentimentLabel=ANXIOUS，score=0.75
    并且 escalationLevel 自动升为 HIGH
    并且 会话在管家工作台中优先显示

  场景: 识别愤怒情绪
    当 张芳 发送消息 "我已经等了半小时了！"
    那么 系统分析 sentimentLabel=ANGRY，score=0.85
    并且 escalationLevel 自动升为 URGENT
    并且 推送通知到主管

  场景: 情绪正常不触发升级
    当 张芳 发送消息 "好的，谢谢"
    那么 sentimentLabel=POSITIVE
    并且 escalationLevel 保持不变

  # ============================================================
  # 场景 5：大模型多轮对话（第三层 AI，P2 阶段）
  # ============================================================

  场景: FAQ 未命中时调用大模型
    假定 用户问题未匹配任何 FAQ
    当 系统调用大模型 API（如 DeepSeek）
    那么 大模型返回多轮对话响应
    并且 消息的 aiAnswerSource 为 "LLM"
    并且 附上免责声明 "以上回答由AI生成，仅供参考"

  # ============================================================
  # 场景 6：知识库管理
  # ============================================================

  场景: 管家搜索知识库
    当 管家 请求 GET /api/v1/chat/knowledge?keyword=孕期&page=1&size=10
    那么 返回匹配的知识文章列表，分页

  场景: 管理员新增知识文章
    当 OPS_ADMIN POST /api/v1/admin/knowledge
       {question:"NT检查是什么", answer:"...", category:"PRENATAL", tags:"NT,产检"}
    那么 创建 KnowledgeArticle，status=PUBLISHED
    并且 返回 code=200
