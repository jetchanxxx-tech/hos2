-- ============================================================
-- V4: 投诉管理表 + 响应时长字段
-- ============================================================

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '投诉用户ID',
    session_id BIGINT NULL COMMENT '关联会话ID',
    category VARCHAR(32) NOT NULL DEFAULT 'GENERAL' COMMENT '投诉分类',
    content TEXT NOT NULL COMMENT '投诉内容',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/RESOLVED/CLOSED',
    priority VARCHAR(8) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    assigned_to BIGINT NULL COMMENT '指派人ID',
    resolution TEXT NULL COMMENT '处理方案',
    resolved_at DATETIME NULL COMMENT '处理时间',
    resolved_by BIGINT NULL COMMENT '处理人ID',
    closed_at DATETIME NULL COMMENT '关闭时间',
    satisfaction_score INT NULL COMMENT '投诉处理满意度 1-5',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_assigned_to (assigned_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉管理表';

-- 为 chat_sessions 添加满意度评分字段（如果不存在）
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS satisfaction_score INT NULL COMMENT '满意度 1-5';
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS first_response_seconds INT NULL COMMENT '首次响应时间（秒）';
