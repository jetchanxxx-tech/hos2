-- ============================================================
-- 惠福星链 · Huifu StarChain — 初始化数据库架构
-- Target: MySQL 8.0+ Community Edition
-- Engine: InnoDB, Charset: utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 用户表 · users
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `openid`           VARCHAR(128)    DEFAULT NULL             COMMENT '微信小程序 openid',
    `unionid`          VARCHAR(128)    DEFAULT NULL             COMMENT '微信 unionid',
    `wecom_id`         VARCHAR(128)    DEFAULT NULL             COMMENT '企业微信 userid',
    `phone`            VARCHAR(256)    NOT NULL                 COMMENT '手机号（AES-256 加密存储）',
    `phone_hash`       CHAR(64)        NOT NULL                 COMMENT '手机号 SHA-256 哈希（用于查询）',
    `name_masked`      VARCHAR(64)     NOT NULL                 COMMENT '脱敏姓名（如 张*芳）',
    `real_name`        VARCHAR(256)    DEFAULT NULL             COMMENT '真实姓名（AES-256 加密存储）',
    `id_card_enc`      VARCHAR(512)    DEFAULT NULL             COMMENT '身份证号（AES-256 加密）',
    `id_card_hash`     CHAR(64)        DEFAULT NULL             COMMENT '身份证 SHA-256 哈希',
    `gender`           TINYINT         DEFAULT 0                COMMENT '性别: 0=未知, 1=男, 2=女',
    `birth_date`       DATE            DEFAULT NULL             COMMENT '出生日期',
    `avatar_url`       VARCHAR(512)    DEFAULT NULL             COMMENT '头像URL',
    `role`             VARCHAR(24)     NOT NULL DEFAULT 'RESIDENT' COMMENT '角色: RESIDENT/BUTLER_MEDICAL/BUTLER_SERVICE/HOSPITAL_ADMIN/OPS_ADMIN/SUPER_ADMIN',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/SUSPENDED',
    `family_id`        BIGINT          DEFAULT NULL             COMMENT '所属家庭ID',
    `hospital_patient_id` VARCHAR(128) DEFAULT NULL             COMMENT '医院HIS患者ID',
    `data_auth_consent` TINYINT       NOT NULL DEFAULT 0       COMMENT '数据授权: 0=未授权, 1=已授权, 2=已撤销',
    `last_login_at`    DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
    `last_login_ip`    VARCHAR(64)     DEFAULT NULL             COMMENT '最后登录IP',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone_hash` (`phone_hash`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_wecom_id` (`wecom_id`),
    KEY `idx_role` (`role`),
    KEY `idx_family_id` (`family_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 家庭表 · families
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `families` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '家庭ID（同心圆ID）',
    `family_name`      VARCHAR(128)    NOT NULL                 COMMENT '家庭名称',
    `primary_user_id`  BIGINT          NOT NULL                 COMMENT '主账号用户ID（通常是孕妈）',
    `member_count`     INT             NOT NULL DEFAULT 1       COMMENT '家庭成员数',
    `invite_code`      VARCHAR(16)     DEFAULT NULL             COMMENT '家庭邀请码（唯一）',
    `share_policy`     VARCHAR(16)     NOT NULL DEFAULT 'PRIMARY_ONLY' COMMENT '共享策略: PRIMARY_ONLY/FULL_SHARE/CUSTOM',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISSOLVED',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_primary_user_id` (`primary_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表（同心圆核心）';

-- -----------------------------------------------------------
-- 3. 家庭成员关联表 · family_members
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `family_members` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `family_id`        BIGINT          NOT NULL                 COMMENT '家庭ID',
    `user_id`          BIGINT          NOT NULL                 COMMENT '成员用户ID',
    `relationship`     VARCHAR(16)     NOT NULL DEFAULT 'SELF'  COMMENT '关系: SELF/SPOUSE/CHILD/PARENT/OTHER',
    `share_scope`      VARCHAR(32)     NOT NULL DEFAULT 'ALL'   COMMENT '共享范围: ALL/REPORT_ONLY/BASIC_ONLY/NONE',
    `is_emergency_contact` TINYINT     NOT NULL DEFAULT 0       COMMENT '是否紧急联系人',
    `joined_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭成员关联表';

-- -----------------------------------------------------------
-- 4. 健康档案 — 时光轴事件 · health_records
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `health_records` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT          NOT NULL                 COMMENT '所属用户ID',
    `record_type`      VARCHAR(32)     NOT NULL                 COMMENT '记录类型: CHECKUP/REPORT/FOLLOWUP/IMMUNIZATION/SURGERY/DELIVERY/SELF_RECORD/MILESTONE',
    `event_date`       DATE            NOT NULL                 COMMENT '事件发生日期',
    `event_time`       TIME            DEFAULT NULL             COMMENT '事件时间',
    `gestational_week` VARCHAR(8)      DEFAULT NULL             COMMENT '孕周（孕期适用）如 12w3d',
    `event_title`      VARCHAR(256)    NOT NULL                 COMMENT '事件标题',
    `event_summary`    TEXT            DEFAULT NULL             COMMENT '事件摘要',
    `detail_json`      JSON            DEFAULT NULL             COMMENT '详细数据（JSON格式）',
    `source`           VARCHAR(16)     NOT NULL DEFAULT 'MANUAL' COMMENT '数据来源: HIS/LIS/PACS/EMR/WUJIAN/MANUAL/DEVICE/AI',
    `source_ref_id`    VARCHAR(128)    DEFAULT NULL             COMMENT '来源系统引用ID',
    `hospital_dept`    VARCHAR(128)    DEFAULT NULL             COMMENT '就诊科室',
    `attending_doctor` VARCHAR(64)     DEFAULT NULL             COMMENT '主治医生',
    `abnormal_flag`    TINYINT         NOT NULL DEFAULT 0       COMMENT '异常标识: 0=正常, 1=异常, 2=危急',
    `alert_triggered`  TINYINT         NOT NULL DEFAULT 0       COMMENT '是否触发预警',
    `tags_json`        JSON            DEFAULT NULL             COMMENT '标签（如 ["高血压","妊娠糖尿病"]）',
    `is_deleted`       TINYINT         NOT NULL DEFAULT 0       COMMENT '软删除',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id_date` (`user_id`, `event_date`),
    KEY `idx_record_type` (`record_type`),
    KEY `idx_abnormal` (`abnormal_flag`),
    KEY `idx_source` (`source`),
    KEY `idx_source_ref` (`source_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康档案·时光轴';

-- -----------------------------------------------------------
-- 5. 检验报告指标表 · lab_reports
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `lab_reports` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `health_record_id` BIGINT          NOT NULL                 COMMENT '关联健康档案ID',
    `report_type`      VARCHAR(32)     NOT NULL                 COMMENT '报告类型: LAB/B_ULTRASOUND/IMAGING/PATHOLOGY',
    `indicator_name`   VARCHAR(128)    NOT NULL                 COMMENT '指标名称',
    `indicator_code`   VARCHAR(64)     DEFAULT NULL             COMMENT '指标编码（LOINC/医院编码）',
    `result_value`     VARCHAR(128)    DEFAULT NULL             COMMENT '检测结果',
    `unit`             VARCHAR(32)     DEFAULT NULL             COMMENT '单位',
    `reference_range`  VARCHAR(128)    DEFAULT NULL             COMMENT '参考范围',
    `is_abnormal`      TINYINT         NOT NULL DEFAULT 0       COMMENT '是否异常: 0=正常, 1=偏高, -1=偏低, 2=危急',
    `abnormal_direction` VARCHAR(8)    DEFAULT NULL             COMMENT '异常方向: HIGH/LOW/CRITICAL',
    `report_date`      DATE            NOT NULL                 COMMENT '报告日期',
    `machine_info`     VARCHAR(256)    DEFAULT NULL             COMMENT '仪器信息',
    `trend_data_json`  JSON            DEFAULT NULL             COMMENT '趋势数据（历史值数组）',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`health_record_id`),
    KEY `idx_indicator` (`indicator_code`),
    KEY `idx_report_date` (`report_date`),
    KEY `idx_abnormal` (`is_abnormal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检验/检查报告指标';

-- -----------------------------------------------------------
-- 6. 服务包表 · service_packages
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `service_packages` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `name`             VARCHAR(128)    NOT NULL                 COMMENT '服务包名称',
    `subtitle`         VARCHAR(256)    DEFAULT NULL             COMMENT '副标题/卖点',
    `type`             VARCHAR(16)     NOT NULL                 COMMENT '类型: VIP/VVIP/STANDARD/EXPERIENCE',
    `category`         VARCHAR(32)     NOT NULL DEFAULT 'MATERNITY' COMMENT '分类: MATERNITY/PEDIATRICS/GYNECOLOGY/GENERAL',
    `price`            DECIMAL(12,2)   NOT NULL                 COMMENT '原价（元）',
    `discount_price`   DECIMAL(12,2)   DEFAULT NULL             COMMENT '折后价',
    `duration_days`    INT             NOT NULL DEFAULT 365      COMMENT '有效期（天）',
    `max_beneficiaries` INT            NOT NULL DEFAULT 1        COMMENT '最大受益人数（1=仅本人, 3=2大1小等）',
    `cover_image_url`  VARCHAR(512)    DEFAULT NULL             COMMENT '封面图URL',
    `benefits_json`    JSON            NOT NULL                 COMMENT '权益明细JSON [{name, count, description}]',
    `terms_text`       TEXT            DEFAULT NULL             COMMENT '服务条款',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/ON_SHELF/OFF_SHELF',
    `sort_order`       INT             NOT NULL DEFAULT 0       COMMENT '排序权重',
    `created_by`       BIGINT          DEFAULT NULL             COMMENT '创建人ID',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务包';

-- -----------------------------------------------------------
-- 7. 服务包订单表 · package_orders
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `package_orders` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `order_no`         VARCHAR(32)     NOT NULL                 COMMENT '订单号（HF+时间戳+随机数）',
    `user_id`          BIGINT          NOT NULL                 COMMENT '购买用户ID',
    `family_id`        BIGINT          DEFAULT NULL             COMMENT '关联家庭ID（家庭共享服务包时使用）',
    `package_id`       BIGINT          NOT NULL                 COMMENT '服务包ID',
    `package_snapshot_json` JSON       DEFAULT NULL             COMMENT '购买时服务包快照',
    `amount`           DECIMAL(12,2)   NOT NULL                 COMMENT '实付金额',
    `original_amount`  DECIMAL(12,2)   NOT NULL                 COMMENT '原价',
    `payment_method`   VARCHAR(32)     DEFAULT NULL             COMMENT '支付方式: WECHAT_PAY/ALIPAY/HOSPITAL_DEDUCT/ADMIN_GIFT',
    `payment_trade_no` VARCHAR(128)    DEFAULT NULL             COMMENT '第三方支付交易号',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/ACTIVE/EXPIRED/REFUNDED/CANCELLED',
    `start_date`       DATE            DEFAULT NULL             COMMENT '生效日期',
    `end_date`         DATE            DEFAULT NULL             COMMENT '失效日期',
    `paid_at`          DATETIME        DEFAULT NULL             COMMENT '支付时间',
    `refund_amount`    DECIMAL(12,2)   DEFAULT NULL             COMMENT '退款金额',
    `refund_at`        DATETIME        DEFAULT NULL             COMMENT '退款时间',
    `remark`           VARCHAR(512)    DEFAULT NULL             COMMENT '备注',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_package_id` (`package_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务包订单';

-- -----------------------------------------------------------
-- 8. 权益核销表 · benefit_redemptions
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `benefit_redemptions` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `order_id`         BIGINT          NOT NULL                 COMMENT '关联订单ID',
    `package_id`       BIGINT          NOT NULL                 COMMENT '关联服务包ID',
    `user_id`          BIGINT          NOT NULL                 COMMENT '受益人用户ID（可能不同于购买人）',
    `benefit_type`     VARCHAR(32)     NOT NULL                 COMMENT '权益类型: ESCORT/COUNSEL/CHECKUP/ACTIVITY/EDUCATION/REFERRAL',
    `benefit_name`     VARCHAR(128)    NOT NULL                 COMMENT '权益名称',
    `total_count`      INT             NOT NULL DEFAULT 1       COMMENT '总次数',
    `used_count`       INT             NOT NULL DEFAULT 0       COMMENT '已用次数',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: AVAILABLE/PARTIALLY_USED/EXHAUSTED',
    `qr_code`          VARCHAR(256)    DEFAULT NULL             COMMENT '核销二维码',
    `redeemed_by`      BIGINT          DEFAULT NULL             COMMENT '核销管家ID',
    `redeemed_at`      DATETIME        DEFAULT NULL             COMMENT '最后一次核销时间',
    `redeem_location`  VARCHAR(256)    DEFAULT NULL             COMMENT '核销地点',
    `remark`           VARCHAR(512)    DEFAULT NULL             COMMENT '核销备注',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_benefit_type` (`benefit_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权益核销记录';

-- -----------------------------------------------------------
-- 9. 随访任务表 · followup_tasks
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `followup_tasks` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT          NOT NULL                 COMMENT '随访对象用户ID',
    `family_id`        BIGINT          DEFAULT NULL             COMMENT '关联家庭ID',
    `health_record_id` BIGINT          DEFAULT NULL             COMMENT '触发的健康档案ID',
    `task_type`        VARCHAR(32)     NOT NULL                 COMMENT '任务类型: POST_DISCHARGE/GESTATIONAL/MONTHLY_AGE/VACCINE/ABNORMAL_LAB/CHRONIC_DISEASE/MANUAL',
    `trigger_condition` VARCHAR(256)   DEFAULT NULL             COMMENT '触发条件描述',
    `scheduled_date`   DATE            NOT NULL                 COMMENT '计划随访日期',
    `scheduled_time`   TIME            DEFAULT NULL             COMMENT '计划随访时间',
    `deadline_date`    DATE            DEFAULT NULL             COMMENT '截止日期',
    `priority`         VARCHAR(8)      NOT NULL DEFAULT 'NORMAL' COMMENT '优先级: LOW/NORMAL/HIGH/URGENT',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ASSIGNED/IN_PROGRESS/COMPLETED/OVERDUE/CANCELLED',
    `assigned_butler_id` BIGINT        DEFAULT NULL             COMMENT '指派的管家ID',
    `followup_method`  VARCHAR(16)     NOT NULL DEFAULT 'PHONE' COMMENT '随访方式: PHONE/AI_PHONE/WECOM/IN_PERSON/MINI_PROGRAM',
    `template_id`      VARCHAR(64)     DEFAULT NULL             COMMENT '随访模板ID',
    `completion_note`  TEXT            DEFAULT NULL             COMMENT '完成备注',
    `completion_json`  JSON            DEFAULT NULL             COMMENT '结构化随访结果',
    `ai_generated`     TINYINT         NOT NULL DEFAULT 0       COMMENT '是否AI生成',
    `ai_call_id`       VARCHAR(128)    DEFAULT NULL             COMMENT 'AI外呼任务ID',
    `completed_at`     DATETIME        DEFAULT NULL             COMMENT '完成时间',
    `completed_by`     BIGINT          DEFAULT NULL             COMMENT '完成人ID',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_scheduled` (`user_id`, `scheduled_date`),
    KEY `idx_butler` (`assigned_butler_id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_scheduled_date` (`scheduled_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访任务';

-- -----------------------------------------------------------
-- 10. 会话表（HCRM） · chat_sessions
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `session_no`       VARCHAR(32)     NOT NULL                 COMMENT '会话编号（HS+时间戳）',
    `user_id`          BIGINT          NOT NULL                 COMMENT '用户ID',
    `channel`          VARCHAR(16)     NOT NULL DEFAULT 'MINIPROGRAM' COMMENT '渠道: MINIPROGRAM/WECOM/ADMIN_PANEL',
    `intent_type`      VARCHAR(32)     DEFAULT NULL             COMMENT '意图分类: MEDICAL/BENEFIT/COMPLAINT/GENERAL/APPOINTMENT/FOLLOWUP',
    `intent_confidence` DECIMAL(5,4)   DEFAULT NULL             COMMENT '意图识别置信度',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/WAITING_BUTLER/IN_PROGRESS/RESOLVED/CLOSED',
    `escalation_level` VARCHAR(16)     NOT NULL DEFAULT 'NONE'  COMMENT '升级级别: NONE/AI_HANDLED/BUTLER/SUPERVISOR/URGENT',
    `sentiment_score`  DECIMAL(5,4)    DEFAULT NULL             COMMENT '情绪评分（0~1，越高越负面）',
    `sentiment_label`  VARCHAR(16)     DEFAULT NULL             COMMENT '情绪标签: POSITIVE/NEUTRAL/ANXIOUS/ANGRY',
    `resolved_by`      BIGINT          DEFAULT NULL             COMMENT '解决人ID',
    `resolved_at`      DATETIME        DEFAULT NULL             COMMENT '解决时间',
    `satisfaction_score` TINYINT       DEFAULT NULL             COMMENT '满意度评分 1-5',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_no` (`session_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_channel` (`channel`),
    KEY `idx_status` (`status`),
    KEY `idx_intent` (`intent_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HCRM会话表·惠福灵犀';

-- -----------------------------------------------------------
-- 11. 聊天消息表 · chat_messages
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `session_id`       BIGINT          NOT NULL                 COMMENT '会话ID',
    `seq_no`           INT             NOT NULL DEFAULT 0       COMMENT '消息序号（会话内递增）',
    `sender_type`      VARCHAR(16)     NOT NULL                 COMMENT '发送者类型: USER/BUTLER/AI/SYSTEM',
    `sender_id`        BIGINT          DEFAULT NULL             COMMENT '发送者ID',
    `sender_name`      VARCHAR(64)     DEFAULT NULL             COMMENT '发送者名称',
    `msg_type`         VARCHAR(16)     NOT NULL DEFAULT 'TEXT'  COMMENT '消息类型: TEXT/IMAGE/VOICE/FILE/CARD/TEMPLATE',
    `content`          TEXT            DEFAULT NULL             COMMENT '文本内容',
    `media_url`        VARCHAR(512)    DEFAULT NULL             COMMENT '媒体文件URL',
    `media_meta_json`  JSON            DEFAULT NULL             COMMENT '媒体元信息',
    `extra_json`       JSON            DEFAULT NULL             COMMENT '扩展数据（卡片、模板等）',
    `trigger_alert`    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否触发预警',
    `alert_keyword`    VARCHAR(128)    DEFAULT NULL             COMMENT '触发的预警关键词',
    `ai_answer_source` VARCHAR(64)     DEFAULT NULL             COMMENT 'AI回答来源（FAQ/MODEL/TRANSFER）',
    `is_read`          TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已读',
    `read_at`          DATETIME        DEFAULT NULL             COMMENT '已读时间',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_seq` (`session_id`, `seq_no`),
    KEY `idx_sender` (`sender_type`, `sender_id`),
    KEY `idx_alert` (`trigger_alert`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表·惠福灵犀';

-- -----------------------------------------------------------
-- 12. 仪表盘缓存表 · dashboard_cache
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dashboard_cache` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `metric_name`      VARCHAR(64)     NOT NULL                 COMMENT '指标名称: active_members/revenue/followup_rate/satisfaction/...',
    `metric_value`     DECIMAL(18,4)   NOT NULL                 COMMENT '指标值',
    `metric_label`     VARCHAR(128)    DEFAULT NULL             COMMENT '指标显示名',
    `dimension`        VARCHAR(32)     NOT NULL DEFAULT 'OVERALL' COMMENT '维度: OVERALL/DAILY/WEEKLY/MONTHLY/BY_DEPT/BY_PACKAGE',
    `dimension_value`  VARCHAR(128)    DEFAULT NULL             COMMENT '维度值',
    `period_start`     DATE            DEFAULT NULL             COMMENT '统计周期起始',
    `period_end`       DATE            DEFAULT NULL             COMMENT '统计周期结束',
    `delta_value`      DECIMAL(18,4)   DEFAULT NULL             COMMENT '环比变化值',
    `delta_percent`    DECIMAL(8,4)    DEFAULT NULL             COMMENT '环比变化百分比',
    `meta_json`        JSON            DEFAULT NULL             COMMENT '扩展元数据',
    `calculated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_metric_dim` (`metric_name`, `dimension`, `dimension_value`(64), `period_start`, `period_end`),
    KEY `idx_metric_name` (`metric_name`),
    KEY `idx_calculated_at` (`calculated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营仪表盘缓存';

-- -----------------------------------------------------------
-- 13. 审计日志表 · audit_logs
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `trace_id`         VARCHAR(64)     DEFAULT NULL             COMMENT '分布式追踪ID',
    `user_id`          BIGINT          DEFAULT NULL             COMMENT '操作用户ID',
    `username`         VARCHAR(64)     DEFAULT NULL             COMMENT '操作用户名',
    `user_role`        VARCHAR(24)     DEFAULT NULL             COMMENT '操作时角色',
    `action`           VARCHAR(64)     NOT NULL                 COMMENT '操作动作: CREATE/READ/UPDATE/DELETE/EXPORT/LOGIN/LOGOUT',
    `resource_type`    VARCHAR(64)     NOT NULL                 COMMENT '资源类型: USER/FAMILY/HEALTH_RECORD/ORDER/PACKAGE/...',
    `resource_id`      VARCHAR(128)    DEFAULT NULL             COMMENT '资源ID',
    `resource_desc`    VARCHAR(256)    DEFAULT NULL             COMMENT '资源描述',
    `old_value_json`   JSON            DEFAULT NULL             COMMENT '变更前值',
    `new_value_json`   JSON            DEFAULT NULL             COMMENT '变更后值',
    `ip_address`       VARCHAR(64)     DEFAULT NULL             COMMENT '操作IP',
    `user_agent`       VARCHAR(512)    DEFAULT NULL             COMMENT 'User-Agent',
    `request_url`      VARCHAR(512)    DEFAULT NULL             COMMENT '请求URL',
    `request_method`   VARCHAR(8)      DEFAULT NULL             COMMENT 'HTTP方法',
    `duration_ms`      INT             DEFAULT NULL             COMMENT '请求耗时（毫秒）',
    `result`           VARCHAR(16)     NOT NULL DEFAULT 'SUCCESS' COMMENT '结果: SUCCESS/FAILURE/DENIED',
    `error_msg`        VARCHAR(1024)   DEFAULT NULL             COMMENT '错误信息',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志（合规）';

-- -----------------------------------------------------------
-- 14. 医院网关同步日志 · hospital_gateway_sync
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hospital_gateway_sync` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `data_type`        VARCHAR(16)     NOT NULL                 COMMENT '数据类型: HIS/LIS/PACS/EMR/WUJIAN',
    `external_id`      VARCHAR(128)    NOT NULL                 COMMENT '医院系统数据ID',
    `batch_no`         VARCHAR(64)     DEFAULT NULL             COMMENT '批次号',
    `sync_direction`   VARCHAR(8)      NOT NULL DEFAULT 'INBOUND' COMMENT '同步方向: INBOUND/OUTBOUND',
    `sync_status`      VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/SUCCESS/FAILED/RETRYING',
    `raw_payload_hash` CHAR(64)        DEFAULT NULL             COMMENT '原始数据SHA-256',
    `processed_json`   JSON            DEFAULT NULL             COMMENT '处理后数据',
    `error_message`    TEXT            DEFAULT NULL             COMMENT '错误信息',
    `retry_count`      INT             NOT NULL DEFAULT 0       COMMENT '重试次数',
    `synced_at`        DATETIME        DEFAULT NULL             COMMENT '同步完成时间',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_id` (`data_type`, `external_id`),
    KEY `idx_batch_no` (`batch_no`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院数据网关同步日志';

-- -----------------------------------------------------------
-- 15. 知识库表 · knowledge_articles
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `knowledge_articles` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `title`            VARCHAR(256)    NOT NULL                 COMMENT '标题',
    `category`         VARCHAR(32)     NOT NULL                 COMMENT '分类: PREGNANCY/POSTPARTUM/PEDIATRICS/GYNECOLOGY/NUTRITION/VACCINE/GENERAL',
    `tags_json`        JSON            DEFAULT NULL             COMMENT '标签',
    `question`         VARCHAR(512)    NOT NULL                 COMMENT '标准问题（FAQ匹配用）',
    `answer`           TEXT            NOT NULL                 COMMENT '标准回答',
    `answer_type`      VARCHAR(16)     NOT NULL DEFAULT 'TEXT'  COMMENT '回答类型: TEXT/RICH_HTML/VIDEO/CARD',
    `media_urls_json`  JSON            DEFAULT NULL             COMMENT '附件媒体URL列表',
    `view_count`       INT             NOT NULL DEFAULT 0       COMMENT '浏览次数',
    `helpful_count`    INT             NOT NULL DEFAULT 0       COMMENT '有用次数',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: DRAFT/PUBLISHED/ARCHIVED',
    `created_by`       BIGINT          DEFAULT NULL             COMMENT '创建人ID',
    `published_at`     DATETIME        DEFAULT NULL             COMMENT '发布时间',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    FULLTEXT KEY `ft_question_answer` (`question`, `answer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库（FAQ）';

-- -----------------------------------------------------------
-- 16. 邀请记录表 · invite_records
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `invite_records` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT,
    `inviter_id`       BIGINT          NOT NULL                 COMMENT '邀请人用户ID',
    `invite_code`      VARCHAR(16)     NOT NULL                 COMMENT '邀请码',
    `invite_channel`   VARCHAR(32)     DEFAULT NULL             COMMENT '邀请渠道: WECHAT_SHARE/QR_CODE/ORAL',
    `invited_phone_hash` CHAR(64)      DEFAULT NULL             COMMENT '被邀请人手机号哈希',
    `invited_user_id`  BIGINT          DEFAULT NULL             COMMENT '注册后的被邀请用户ID',
    `reward_points`    INT             NOT NULL DEFAULT 0       COMMENT '奖励积分',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/REGISTERED/REWARDED/EXPIRED',
    `registered_at`    DATETIME        DEFAULT NULL             COMMENT '被邀请人注册时间',
    `rewarded_at`      DATETIME        DEFAULT NULL             COMMENT '发奖时间',
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_inviter_id` (`inviter_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请裂变记录';

SET FOREIGN_KEY_CHECKS = 1;
