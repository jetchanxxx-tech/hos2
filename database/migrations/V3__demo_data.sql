-- ============================================================
-- V3 Demo 演示数据：为 13800001111 创建全方位演示数据
-- 覆盖：时光轴(健康记录+报告)、同心圆(家庭)、灵犀(聊天)、服务包(订单+权益)、随访
-- ============================================================

SET @DEMO_PHONE = '13800001111';
SET @DEMO_HASH = SHA2('13800001111', 256);
SET @NOW = NOW();

-- ============================================================
-- 1. 演示用户（如已存在则跳过）
-- ============================================================
INSERT IGNORE INTO users (id, phone, phone_hash, name_masked, real_name, gender, birth_date, avatar_url, role, status, data_auth_consent, created_at, updated_at)
VALUES (200, 'demo_enc_13800001111', @DEMO_HASH, '李*芳', 'demo_enc_李丽芳', 0, '1994-05-20', NULL, 'RESIDENT', 'ACTIVE', 1, @NOW, @NOW);

-- 创建配偶和子女
INSERT IGNORE INTO users (id, phone, phone_hash, name_masked, real_name, gender, birth_date, avatar_url, role, status, data_auth_consent, created_at, updated_at)
VALUES
(201, 'demo_enc_spouse', SHA2('spouse_demo', 256), '陈*明', 'demo_enc_陈伟明', 1, '1992-10-15', NULL, 'RESIDENT', 'ACTIVE', 1, @NOW, @NOW),
(202, 'demo_enc_child', SHA2('child_demo', 256), '陈*宝', 'demo_enc_陈小宝', 0, '2025-12-01', NULL, 'RESIDENT', 'ACTIVE', 0, @NOW, @NOW);

-- 创建管家账号
INSERT IGNORE INTO users (id, phone, phone_hash, name_masked, real_name, gender, role, status, data_auth_consent, created_at, updated_at)
VALUES
(203, 'demo_enc_butler1', SHA2('butler1', 256), '刘*霞', 'demo_enc_刘美霞', 0, 'BUTLER_MEDICAL', 'ACTIVE', 1, @NOW, @NOW),
(204, 'demo_enc_butler2', SHA2('butler2', 256), '陈*华', 'demo_enc_陈志华', 1, 'BUTLER_SERVICE', 'ACTIVE', 1, @NOW, @NOW),
(205, 'demo_enc_ops', SHA2('ops_demo', 256), '系统管理员', 'demo_enc_admin', 0, 'OPS_ADMIN', 'ACTIVE', 1, @NOW, @NOW);

-- ============================================================
-- 2. 同心圆 - 家庭 & 成员
-- ============================================================
INSERT IGNORE INTO families (id, family_name, primary_user_id, member_count, invite_code, share_policy, status, created_at, updated_at)
VALUES (200, '李丽芳家庭', 200, 3, 'HFDEMO001', 'FULL_SHARE', 'ACTIVE', @NOW, @NOW);

INSERT IGNORE INTO family_members (family_id, user_id, relationship, share_scope, is_emergency_contact, joined_at)
VALUES
(200, 200, 'SELF',   'ALL',          0, @NOW),
(200, 201, 'SPOUSE', 'ALL',          1, @NOW),
(200, 202, 'CHILD',  'REPORT_ONLY',  0, @NOW);

UPDATE users SET family_id = 200 WHERE id IN (200, 201, 202);

-- ============================================================
-- 3. 时光轴 - 健康记录（从备孕到产后，模拟真实数据）
-- ============================================================

-- 3.1 产检记录（约 12 次）
INSERT IGNORE INTO health_records (user_id, record_type, event_date, event_time, gestational_week, event_title, event_summary, detail_json, source, hospital_dept, attending_doctor, abnormal_flag, created_at, updated_at)
VALUES
-- 备孕期
(200, 'PREGNANCY_PREP', '2025-01-15', '09:00:00', NULL, '备孕咨询', '经体检评估，建议补充叶酸，定期监测排卵', '{"叶酸": "0.4mg/日", "BMI": 22.1}', 'HIS', '妇科', '王医生', 0, @NOW, @NOW),
-- 确认妊娠
(200, 'CHECKUP', '2025-04-20', '08:30:00', '5W',  '首次产检-确认妊娠', '血HCG 阳性，B超见孕囊，宫内早孕', '{"HCG": 12500, "孕囊": "18mm"}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- NT检查
(200, 'CHECKUP', '2025-06-01', '08:00:00', '11W', 'NT 检查', 'NT 1.2mm，正常范围内', '{"NT": 1.2, "CRL": 48}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 唐氏筛查
(200, 'CHECKUP', '2025-06-22', '08:30:00', '14W', '唐氏筛查', '21三体 1:85000，低风险', '{"21三体": "1:85000", "18三体": "1:100000"}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 系统B超
(200, 'CHECKUP', '2025-07-28', '10:00:00', '20W', '系统B超-大排畸', '胎儿结构未见明显异常', '{"BPD": 49, "FL": 32, "AC": 161}', 'HIS', 'B超室', '李医生', 0, @NOW, @NOW),
-- 糖耐量
(200, 'CHECKUP', '2025-09-05', '07:30:00', '25W', '葡萄糖耐量试验', '空腹 4.8，1h 10.2（临界偏高），2h 7.5', '{"空腹": 4.8, "1h": 10.2, "2h": 7.5}', 'LIS', '检验科', NULL, 1, @NOW, @NOW),
-- 糖耐量复查
(200, 'CHECKUP', '2025-09-19', '07:30:00', '27W', '糖耐量复查', '空腹 4.6，1h 8.1，2h 6.3，已恢复正常', '{"空腹": 4.6, "1h": 8.1, "2h": 6.3}', 'LIS', '检验科', NULL, 0, @NOW, @NOW),
-- 孕晚期B超
(200, 'CHECKUP', '2025-10-20', '10:00:00', '31W', '孕晚期B超', '胎儿发育正常，羊水量正常', '{"BPD": 79, "FL": 58, "AC": 270, "AFI": 12}', 'HIS', 'B超室', '李医生', 0, @NOW, @NOW),
-- 胎心监护
(200, 'CHECKUP', '2025-11-10', '09:00:00', '34W', '胎心监护', 'NST 反应型，胎心基线 140bpm，变异好', '{"FHR": 140, "variability": "正常", "accelerations": 3}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 产前评估
(200, 'CHECKUP', '2025-11-25', '08:30:00', '36W', '产前综合评估', '骨盆测量正常，预估胎儿体重 2800g，建议自然分娩', '{"骨盆": "正常", "预估体重": 2800, "胎位": "LOA"}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 分娩
(200, 'DELIVERY', '2025-12-01', '03:20:00', '37W', '自然分娩', '顺利分娩一女婴，体重 3050g，身长 50cm，Apgar 9-10-10', '{"方式": "自然分娩", "性别": "女", "体重": 3050, "身长": 50, "Apgar": "9-10-10"}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 产后42天
(200, 'POSTPARTUM', '2026-01-12', '09:00:00', '产后42天', '产后42天复查', '子宫恢复良好，切口愈合佳，建议盆底肌康复训练', '{"子宫": "恢复正常", "切口": "愈合良好", "建议": "盆底康复"}', 'HIS', '产科', '张主任', 0, @NOW, @NOW),
-- 宝宝42天
(202, 'CHILD_CHECKUP', '2026-01-12', '10:00:00', '42天', '新生儿42天体检', '体格发育正常，心肺听诊无异常，髋关节B超待查', '{"体重": 4200, "身长": 54, "头围": 37}', 'HIS', '儿保科', '赵医生', 0, @NOW, @NOW),
-- 宝宝3个月
(202, 'CHILD_CHECKUP', '2026-03-01', '09:30:00', '3月', '宝宝3月龄体检', '发育达标，已能抬头45度，追视良好', '{"体重": 5800, "身长": 61, "抬头": "45度"}', 'HIS', '儿保科', '赵医生', 0, @NOW, @NOW),
-- 产后康复
(200, 'REHABILITATION', '2026-02-15', '10:00:00', '产后10周', '盆底肌康复第一次', '盆底肌评估 III 级，开始电刺激治疗', '{"评估": "III级", "治疗": "电刺激 20min"}', 'MANUAL', '康复科', '孙医生', 0, @NOW, @NOW),
-- 居家自录
(200, 'SELF_RECORD', '2026-03-20', '08:00:00', NULL, '自测血压', '晨起血压 118/75 mmHg，体重 52kg', '{"血压": "118/75", "体重": 52}', 'MANUAL', NULL, NULL, 0, @NOW, @NOW),
(200, 'SELF_RECORD', '2026-04-05', '08:00:00', NULL, '自测血压', '晨起血压 120/78 mmHg', '{"血压": "120/78"}', 'MANUAL', NULL, NULL, 0, @NOW, @NOW),
-- 疫苗接种
(202, 'VACCINATION', '2026-02-01', '14:00:00', NULL, '乙肝疫苗第二针', '接种后观察30分钟无异常', '{"疫苗": "乙肝", "针次": 2, "批号": "HB2026001"}', 'HIS', '计免科', NULL, 0, @NOW, @NOW);

-- ============================================================
-- 4. 检验报告
-- ============================================================
INSERT IGNORE INTO lab_reports (health_record_id, user_id, indicator_code, indicator_name, result_value, unit, reference_min, reference_max, is_abnormal, collected_at, created_at, updated_at)
VALUES
-- NT 期血常规
(3, 200, 'WBC', '白细胞计数', '8.5', '10^9/L', 3.5, 9.5, 0, '2025-06-01 08:00', @NOW, @NOW),
(3, 200, 'HGB', '血红蛋白', '118', 'g/L', 110, 150, 0, '2025-06-01 08:00', @NOW, @NOW),
(3, 200, 'PLT', '血小板', '230', '10^9/L', 125, 350, 0, '2025-06-01 08:00', @NOW, @NOW),
-- 糖耐量异常
(6, 200, 'FPG', '空腹血糖', '4.8', 'mmol/L', 3.9, 5.1, 0, '2025-09-05 07:30', @NOW, @NOW),
(6, 200, 'OGTT_1H', '1小时血糖', '10.2', 'mmol/L', 0, 10.0, 1, '2025-09-05 08:30', @NOW, @NOW),
(6, 200, 'OGTT_2H', '2小时血糖', '7.5', 'mmol/L', 0, 8.5, 0, '2025-09-05 09:30', @NOW, @NOW),
-- 糖耐量复查正常
(7, 200, 'FPG', '空腹血糖', '4.6', 'mmol/L', 3.9, 5.1, 0, '2025-09-19 07:30', @NOW, @NOW),
(7, 200, 'OGTT_1H', '1小时血糖', '8.1', 'mmol/L', 0, 10.0, 0, '2025-09-19 08:30', @NOW, @NOW),
(7, 200, 'OGTT_2H', '2小时血糖', '6.3', 'mmol/L', 0, 8.5, 0, '2025-09-19 09:30', @NOW, @NOW),
-- 产后血常规
(12, 200, 'WBC', '白细胞计数', '6.2', '10^9/L', 3.5, 9.5, 0, '2026-01-12 09:00', @NOW, @NOW),
(12, 200, 'HGB', '血红蛋白', '105', 'g/L', 110, 150, 1, '2026-01-12 09:00', @NOW, @NOW);

-- ============================================================
-- 5. 随访任务
-- ============================================================
INSERT IGNORE INTO followup_tasks (id, assigned_butler_id, user_id, name, description, scheduled_date, status, priority, followup_method, trigger_condition, created_at, updated_at)
VALUES
(101, 203, 200, '糖耐量异常随访', '1h血糖10.2偏高，需电话随访饮食控制情况', '2025-09-06', 'COMPLETED', 2, 'PHONE', 'lab_abnormal:OGTT_1H', @NOW, @NOW),
(102, 203, 200, '产后42天复查提醒', '微信提醒产后42天复查预约', '2026-01-05', 'COMPLETED', 1, 'WECHAT', 'time:42d_postpartum', @NOW, @NOW),
(103, 203, 200, '产后盆底康复随访', '电话确认盆底康复训练执行情况', '2026-02-20', 'ASSIGNED', 1, 'PHONE', 'time:10w_postpartum', @NOW, @NOW),
(104, 204, 200, 'VIP服务体验回访', '产后服务包体验满意度问卷', '2026-03-01', 'PENDING', 1, 'PHONE', 'time:3m_postpartum', @NOW, @NOW),
(105, 203, 202, '宝宝3月龄体检提醒', '提醒带宝宝去儿保科做3月龄体检', '2026-02-25', 'PENDING', 1, 'WECHAT', 'time:3m_birth', @NOW, @NOW);

-- ============================================================
-- 6. 灵犀 - 聊天会话 & 消息
-- ============================================================
INSERT IGNORE INTO chat_sessions (id, session_no, user_id, channel, intent_type, intent_confidence, sentiment_score, sentiment_label, status, escalation_level, created_at, updated_at)
VALUES
(200, 'HS20250906090001', 200, 'MINIPROGRAM', 'MEDICAL', 0.85, 0.3, 'ANXIOUS', 'CLOSED', 'NONE', '2025-09-06 09:00:00', @NOW),
(201, 'HS20260112090002', 200, 'WECHAT', 'BENEFIT', 0.72, 0.15, 'POSITIVE', 'CLOSED', 'NONE', '2026-01-12 09:00:00', @NOW);

INSERT IGNORE INTO chat_messages (session_id, seq_no, sender_type, sender_id, sender_name, msg_type, content, trigger_alert, alert_keyword, ai_answer_source, is_read, created_at, updated_at)
VALUES
-- 会话200：糖耐量异常咨询
(200, 1, 'USER', 200, '李丽芳', 'TEXT', '医生你好，我的糖耐量1小时血糖是10.2，这个严重吗？', 0, NULL, NULL, 1, '2025-09-06 09:00:00', @NOW),
(200, 2, 'AI', NULL, '惠福小助手', 'TEXT', '您好！1小时血糖 ≥ 10.0 mmol/L 属于妊娠期糖尿病诊断标准之一。建议：(1) 控制饮食，少食多餐 (2) 避免高糖食物 (3) 每天监测血糖。我们会安排管家与您联系。', 0, NULL, 'FAQ', 1, '2025-09-06 09:00:05', @NOW),
(200, 3, 'USER', 200, '李丽芳', 'TEXT', '好的，我有点担心会影响宝宝', 0, NULL, NULL, 1, '2025-09-06 09:01:00', @NOW),
(200, 4, 'BUTLER', 203, '刘美霞', 'TEXT', '李女士您好，我是您的医疗管家。请不要担心，我们会在孕期全程指导饮食控制。下周来院复查糖耐量即可。', 0, NULL, NULL, 1, '2025-09-06 09:15:00', @NOW),
(200, 5, 'USER', 200, '李丽芳', 'TEXT', '谢谢您！那我就放心了', 0, NULL, NULL, 1, '2025-09-06 09:16:00', @NOW),
-- 会话201：服务包咨询
(201, 1, 'USER', 200, '李丽芳', 'TEXT', '我想问下我的VIP服务包里面，宝宝的体检能用吗？', 0, NULL, NULL, 1, '2026-01-12 09:00:00', @NOW),
(201, 2, 'AI', NULL, '惠福小助手', 'TEXT', '您的VIP孕产全程护航包含产后42天复查（妈妈和宝宝），以及宝宝1岁内的儿保体检3次。可以前往"我的服务包"查看详情和剩余次数。', 0, NULL, 'FAQ', 1, '2026-01-12 09:00:05', @NOW),
(201, 3, 'USER', 200, '李丽芳', 'TEXT', '太好了！那我要怎么预约呢？', 0, NULL, NULL, 1, '2026-01-12 09:01:00', @NOW),
(201, 4, 'BUTLER', 204, '陈志华', 'TEXT', '李女士您好，我是您的服务管家。我们可以在小程序一键预约，也可以让我帮您安排。您偏好哪个时间？', 0, NULL, NULL, 1, '2026-01-12 09:10:00', @NOW);

-- ============================================================
-- 7. 服务包 & 订单 & 权益
-- ============================================================
INSERT IGNORE INTO service_packages (id, name, subtitle, type, category, price, discount_price, duration_days, max_beneficiaries, cover_image_url, benefits_json, terms_text, status, sort_order, created_by, created_at, updated_at)
VALUES
(200, 'VIP 孕产全程护航', '从备孕到产后42天 · 全程专属管家', 'VIP', 'MATERNITY', 12800.00, 9800.00, 365, 1, NULL,
 '[{"name":"产检陪诊","count":12,"desc":"专人陪同产检"},{"name":"专家咨询","count":10,"desc":"三甲专家在线咨询"},{"name":"产后康复","count":5,"desc":"盆底肌康复服务"},{"name":"宝宝体检","count":3,"desc":"儿保体检服务"}]',
 '有效期365天，自购买之日起计算', 'ON_SHELF', 1, 205, @NOW, @NOW),
(201, 'VVIP 全家乐享', '一家三口全覆盖 · 升级VIP体验', 'VVIP', 'MATERNITY', 22800.00, 19800.00, 365, 3, NULL,
 '[{"name":"产检陪诊","count":12,"desc":"专人陪同产检"},{"name":"专家咨询","count":"无限","desc":"三甲专家在线咨询"},{"name":"产后康复","count":10,"desc":"盆底肌康复服务"},{"name":"宝宝体检","count":"无限","desc":"儿保体检服务"},{"name":"心理咨询","count":5,"desc":"产后心理健康服务"}]',
 '有效期365天，支持家庭成员共享', 'ON_SHELF', 2, 205, @NOW, @NOW);

INSERT IGNORE INTO package_orders (id, user_id, family_id, package_id, order_no, amount, discount_amount, final_amount, payment_method, status, paid_at, valid_from, valid_until, created_at, updated_at)
VALUES
(200, 200, 200, 200, 'ORD20250401001', 12800.00, 3000.00, 9800.00, 'WECHAT_PAY', 'PAID', '2025-04-01 10:00:00', '2025-04-01', '2026-03-31', @NOW, @NOW);

INSERT IGNORE INTO benefit_redemptions (id, order_id, user_id, family_id, benefit_name, total_count, used_count, status, last_used_at, created_at, updated_at)
VALUES
(200, 200, 200, 200, '产检陪诊', 12, 5, 'ACTIVE', '2025-10-20', @NOW, @NOW),
(201, 200, 200, 200, '专家咨询', 10, 3, 'ACTIVE', '2025-11-10', @NOW, @NOW),
(202, 200, 200, 200, '产后康复', 5, 1, 'ACTIVE', '2026-02-15', @NOW, @NOW),
(203, 200, 200, 200, '宝宝体检', 3, 1, 'ACTIVE', '2026-03-01', @NOW, @NOW);

-- ============================================================
-- 8. 知识库资料
-- ============================================================
INSERT IGNORE INTO knowledge_articles (id, title, category, tags_json, question, answer, answer_type, status, view_count, helpful_count, created_by, published_at, created_at, updated_at)
VALUES
(200, '妊娠期糖尿病饮食指南', 'PRENATAL', '["妊娠","糖尿病","饮食"]', '妊娠期糖尿病应该怎么控制饮食？', '妊娠期糖尿病饮食控制要点：(1)少食多餐，每日5-6餐 (2)控制碳水化合物摄入，优先选择粗粮 (3)避免甜食和含糖饮料 (4)每餐搭配优质蛋白和蔬菜 (5)每天监测空腹和餐后2h血糖，目标空腹<5.3，餐后2h<6.7', 'TEXT', 'PUBLISHED', 1250, 89, 205, '2025-04-01', @NOW, @NOW),
(201, '产后42天复查须知', 'POSTPARTUM', '["产后","42天","检查"]', '产后42天复查需要检查什么？', '产后42天复查内容包括：(1)全身检查：血压、体重、血常规 (2)妇科检查：子宫恢复、切口/会阴愈合 (3)盆底功能评估 (4)母乳喂养指导 (5)避孕咨询。建议带上出院小结和宝宝一起去儿保科。', 'TEXT', 'PUBLISHED', 980, 67, 205, '2025-05-15', @NOW, @NOW),
(202, 'NT检查是什么', 'PRENATAL', '["NT","孕检","B超"]', 'NT检查是什么？什么时候做？', 'NT（颈项透明层）检查是通过B超测量胎儿颈后透明层厚度，是早孕期重要的排畸筛查。最佳检查时间：孕11-13+6周。正常值：<3.0mm，数值偏高提示染色体异常风险增加，需进一步检查。', 'TEXT', 'PUBLISHED', 2100, 156, 205, '2025-03-01', @NOW, @NOW),
(203, '宝宝疫苗接种时间表', 'CHILD_HEALTH', '["宝宝","疫苗","接种"]', '宝宝需要打哪些疫苗？', '国家免疫规划疫苗时间表：出生：乙肝第1针、卡介苗；1月龄：乙肝第2针；2月龄：脊灰第1针；3月龄：脊灰第2针、百白破第1针；4月龄：脊灰第3针、百白破第2针；5月龄：百白破第3针；6月龄：乙肝第3针；8月龄：麻疹疫苗', 'TEXT', 'PUBLISHED', 3200, 210, 205, '2025-06-01', @NOW, @NOW);

-- ============================================================
-- 9. 审计日志（演示用）
-- ============================================================
INSERT IGNORE INTO audit_logs (id, user_id, username, user_role, action, resource_type, resource_id, resource_desc, ip_address, request_url, result, created_at, updated_at)
VALUES
(200, 200, '李丽芳', 'RESIDENT', 'REGISTER', 'USER', '200', '用户注册', '127.0.0.1', '/api/v1/auth/register', 'SUCCESS', '2025-04-01 08:00:00', @NOW),
(201, 200, '李丽芳', 'RESIDENT', 'CREATE_FAMILY', 'FAMILY', '200', '创建家庭', '127.0.0.1', '/api/v1/families', 'SUCCESS', '2025-04-01 08:05:00', @NOW),
(202, 200, '李丽芳', 'RESIDENT', 'PURCHASE_PACKAGE', 'PACKAGE_ORDER', '200', '购买VIP服务包', '127.0.0.1', '/api/v1/packages/orders', 'SUCCESS', '2025-04-01 10:00:00', @NOW),
(203, 204, '陈志华', 'BUTLER_SERVICE', 'REDEEM_BENEFIT', 'BENEFIT_REDEMPTION', '201', '核销陪诊权益', '127.0.0.1', '/api/v1/benefits/redeem', 'SUCCESS', '2025-09-06 11:00:00', @NOW),
(204, 203, '刘美霞', 'BUTLER_MEDICAL', 'COMPLETE_FOLLOWUP', 'FOLLOWUP_TASK', '101', '完成糖耐量异常随访', '127.0.0.1', '/api/v1/followups/complete', 'SUCCESS', '2025-09-06 15:00:00', @NOW),
(205, 205, '系统管理员', 'OPS_ADMIN', 'ON_SHELF_PACKAGE', 'SERVICE_PACKAGE', '200', '服务包上架', '127.0.0.1', '/api/v1/admin/packages', 'SUCCESS', '2025-04-01 09:00:00', @NOW);

-- ============================================================
-- 10. 邀请记录
-- ============================================================
INSERT IGNORE INTO invite_records (id, family_id, inviter_user_id, invitee_user_id, invite_code, status, invited_at, join_at, created_at, updated_at)
VALUES
(200, 200, 200, 201, 'HFDEMO001', 'JOINED', '2025-04-05 12:00:00', '2025-04-05 12:10:00', @NOW, @NOW);

-- ============================================================
-- 验证
-- ============================================================
SELECT '=== V3 Demo 数据插入完成 ===' AS '';
SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
UNION ALL SELECT 'families', COUNT(*) FROM families
UNION ALL SELECT 'family_members', COUNT(*) FROM family_members
UNION ALL SELECT 'health_records', COUNT(*) FROM health_records
UNION ALL SELECT 'lab_reports', COUNT(*) FROM lab_reports
UNION ALL SELECT 'followup_tasks', COUNT(*) FROM followup_tasks
UNION ALL SELECT 'chat_sessions', COUNT(*) FROM chat_sessions
UNION ALL SELECT 'chat_messages', COUNT(*) FROM chat_messages
UNION ALL SELECT 'service_packages', COUNT(*) FROM service_packages
UNION ALL SELECT 'package_orders', COUNT(*) FROM package_orders
UNION ALL SELECT 'benefit_redemptions', COUNT(*) FROM benefit_redemptions
UNION ALL SELECT 'knowledge_articles', COUNT(*) FROM knowledge_articles
UNION ALL SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL SELECT 'invite_records', COUNT(*) FROM invite_records;
