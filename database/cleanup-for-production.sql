-- ============================================================
-- 惠福星链 · 生产环境数据清理脚本 (MySQL 5.7)
-- 用法: mysql -u root -p huifu_starchain < cleanup-for-production.sql
-- 用途: 上线前清除测试/种子数据，保留表结构，准备接入真实数据
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------
-- 1. 清空业务数据（保留用户表供生产使用）
-- -------------------------------------------------------

-- 清除种子测试用户，保留管理员账号
-- 注意：修改 WHERE 条件指定要保留的用户 ID
DELETE FROM users WHERE id NOT IN (1, 2, 3);
-- DELETE FROM users;  -- 如需清空全部用户

-- 清除家庭和成员关系
DELETE FROM family_members;
DELETE FROM families;

-- 清除健康数据
DELETE FROM health_records;
DELETE FROM lab_reports;

-- 清除服务包订单和权益
DELETE FROM benefit_redemptions;
DELETE FROM package_orders;

-- 清除聊天数据
DELETE FROM chat_messages;
DELETE FROM chat_sessions;

-- 清除随访任务
DELETE FROM followup_tasks;

-- 清除仪表盘缓存
DELETE FROM dashboard_cache;

-- 清除审计日志（可选，生产上线前建议保留）
-- DELETE FROM audit_logs WHERE created_at < '2026-01-01';

-- 清除邀请记录
DELETE FROM invite_records;

-- 清除医院同步记录
DELETE FROM hospital_gateway_sync;

-- -------------------------------------------------------
-- 2. 重置自增 ID（可选）
-- -------------------------------------------------------

ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE families AUTO_INCREMENT = 1;
ALTER TABLE family_members AUTO_INCREMENT = 1;
ALTER TABLE health_records AUTO_INCREMENT = 1;
ALTER TABLE lab_reports AUTO_INCREMENT = 1;
ALTER TABLE service_packages AUTO_INCREMENT = 1;
ALTER TABLE package_orders AUTO_INCREMENT = 1;
ALTER TABLE benefit_redemptions AUTO_INCREMENT = 1;
ALTER TABLE followup_tasks AUTO_INCREMENT = 1;
ALTER TABLE chat_sessions AUTO_INCREMENT = 1;
ALTER TABLE chat_messages AUTO_INCREMENT = 1;
ALTER TABLE dashboard_cache AUTO_INCREMENT = 1;
ALTER TABLE audit_logs AUTO_INCREMENT = 1;
ALTER TABLE invite_records AUTO_INCREMENT = 1;
ALTER TABLE hospital_gateway_sync AUTO_INCREMENT = 1;
ALTER TABLE knowledge_articles AUTO_INCREMENT = 1;

-- -------------------------------------------------------
-- 3. 插入管理员种子账号（数据库初始化的唯一必填项）
-- -------------------------------------------------------

INSERT IGNORE INTO users (id, phone, phone_hash, name_masked, real_name, role, status, data_auth_consent, created_at, updated_at)
VALUES (1, 'encrypted_admin', SHA2('13800000000', 256), '系统管理员', 'encrypted_admin', 'SUPER_ADMIN', 'ACTIVE', 1, NOW(), NOW());

-- -------------------------------------------------------
-- 4. 验证清理结果
-- -------------------------------------------------------

SELECT '=== 清理完成，当前表行数 ===' AS '';
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'families', COUNT(*) FROM families
UNION ALL SELECT 'family_members', COUNT(*) FROM family_members
UNION ALL SELECT 'health_records', COUNT(*) FROM health_records
UNION ALL SELECT 'lab_reports', COUNT(*) FROM lab_reports
UNION ALL SELECT 'service_packages', COUNT(*) FROM service_packages
UNION ALL SELECT 'package_orders', COUNT(*) FROM package_orders
UNION ALL SELECT 'benefit_redemptions', COUNT(*) FROM benefit_redemptions
UNION ALL SELECT 'followup_tasks', COUNT(*) FROM followup_tasks
UNION ALL SELECT 'chat_sessions', COUNT(*) FROM chat_sessions
UNION ALL SELECT 'chat_messages', COUNT(*) FROM chat_messages
UNION ALL SELECT 'dashboard_cache', COUNT(*) FROM dashboard_cache
UNION ALL SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL SELECT 'invite_records', COUNT(*) FROM invite_records
UNION ALL SELECT 'hospital_gateway_sync', COUNT(*) FROM hospital_gateway_sync
UNION ALL SELECT 'knowledge_articles', COUNT(*) FROM knowledge_articles;

SET FOREIGN_KEY_CHECKS = 1;
