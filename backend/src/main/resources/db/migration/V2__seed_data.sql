-- ============================================================
-- 惠福星链 · Seed Data — 演示 & 初始化数据
-- ============================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------
-- 系统管理员
-- -----------------------------------------------------------
INSERT INTO `users` (`id`, `phone`, `phone_hash`, `name_masked`, `role`, `status`, `data_auth_consent`) VALUES
(1, 'encrypted_13800001111', SHA2('13800001111', 256), '张**', 'SUPER_ADMIN', 'ACTIVE', 1),
(2, 'encrypted_13800002222', SHA2('13800002222', 256), '李*明',  'HOSPITAL_ADMIN', 'ACTIVE', 1),
(3, 'encrypted_13800003333', SHA2('13800003333', 256), '王*莉',  'OPS_ADMIN',       'ACTIVE', 1);

-- -----------------------------------------------------------
-- 医疗管家 & 服务管家
-- -----------------------------------------------------------
INSERT INTO `users` (`id`, `phone`, `phone_hash`, `name_masked`, `role`, `status`, `data_auth_consent`) VALUES
(10, 'encrypted_13900001111', SHA2('13900001111', 256), '陈*华', 'BUTLER_MEDICAL',  'ACTIVE', 1),
(11, 'encrypted_13900002222', SHA2('13900002222', 256), '刘*霞', 'BUTLER_MEDICAL',  'ACTIVE', 1),
(12, 'encrypted_13900003333', SHA2('13900003333', 256), '黄*强', 'BUTLER_SERVICE',  'ACTIVE', 1),
(13, 'encrypted_13900004444', SHA2('13900004444', 256), '赵*婷', 'BUTLER_SERVICE',  'ACTIVE', 1);

-- -----------------------------------------------------------
-- 演示家庭 & 星球居民
-- -----------------------------------------------------------
INSERT INTO `families` (`id`, `family_name`, `primary_user_id`, `member_count`, `invite_code`) VALUES
(100, '张芳家庭', 100, 3, 'HF000001'),
(101, '陈婷家庭', 101, 2, 'HF000002');

INSERT INTO `users` (`id`, `phone`, `phone_hash`, `name_masked`, `role`, `family_id`, `status`, `data_auth_consent`, `gender`, `birth_date`) VALUES
(100, 'encrypted_13600001111', SHA2('13600001111', 256), '张*芳', 'RESIDENT', 100, 'ACTIVE', 1, 2, '1993-05-12'),
(101, 'encrypted_13600002222', SHA2('13600002222', 256), '陈*婷', 'RESIDENT', 101, 'ACTIVE', 1, 2, '1995-08-23');

INSERT INTO `family_members` (`family_id`, `user_id`, `relationship`, `share_scope`) VALUES
(100, 100, 'SELF',   'ALL'),
(100, 101, 'SPOUSE', 'ALL'),
(100, 102, 'CHILD',  'REPORT_ONLY'),
(101, 101, 'SELF',   'ALL'),
(101, 103, 'PARENT', 'BASIC_ONLY');

-- -----------------------------------------------------------
-- 演示服务包
-- -----------------------------------------------------------
INSERT INTO `service_packages` (`id`, `name`, `subtitle`, `type`, `category`, `price`, `discount_price`, `duration_days`, `max_beneficiaries`, `benefits_json`, `status`, `sort_order`) VALUES
(1, 'VIP 孕产全程护航', '从备孕到产后42天 · 全程专属管家', 'VIP', 'MATERNITY', 12800.00, 9800.00, 365, 1,
   '[{"name":"产检陪诊","count":12,"description":"全程陪同产检，代排队、代取药"},{"name":"报告解读","count":999,"description":"每次报告出结果后1小时内电话解读"},{"name":"营养指导","count":4,"description":"孕期营养评估与膳食方案"},{"name":"产后42天复查","count":1,"description":"产后42天全面复查+康复指导"},{"name":"线上咨询","count":999,"description":"7×12小时在线医疗咨询"}]',
   'ON_SHELF', 1),
(2, 'VVIP 家庭健康管家', '2大1小 · 全家共享 · 全年无忧', 'VVIP', 'MATERNITY', 22800.00, 18800.00, 365, 3,
   '[{"name":"VIP全部权益","count":1,"description":"包含VIP孕产全程护航所有权益"},{"name":"儿童保健","count":6,"description":"0-3岁儿保体检+疫苗接种提醒"},{"name":"配偶体检","count":1,"description":"配偶全面体检套餐"},{"name":"急诊绿色通道","count":2,"description":"急诊优先安排"},{"name":"专家会诊","count":2,"description":"多学科专家联合会诊"}]',
   'ON_SHELF', 2),
(3, '产后康复套餐', '产后修复 · 科学恢复', 'STANDARD', 'GYNECOLOGY', 6800.00, 5800.00, 180, 1,
   '[{"name":"盆底肌评估","count":1,"description":"盆底肌功能评估"},{"name":"产后康复训练","count":8,"description":"一对一产后康复指导"},{"name":"营养方案","count":2,"description":"产后营养与体重管理"},{"name":"心理咨询","count":3,"description":"产后心理疏导"}]',
   'ON_SHELF', 3),
(4, '儿保成长套餐', '0-3岁全程守护 · 健康成长', 'STANDARD', 'PEDIATRICS', 3800.00, 3200.00, 365, 1,
   '[{"name":"儿保体检","count":6,"description":"按月龄定期儿保体检"},{"name":"疫苗提醒","count":999,"description":"疫苗接种智能提醒"},{"name":"生长发育评估","count":4,"description":"身高体重头围发育评估"},{"name":"喂养指导","count":4,"description":"母乳/辅食/营养指导"}]',
   'ON_SHELF', 4),
(5, '五健筛查套餐', '老年人健康筛查 · 早发现早干预', 'STANDARD', 'GENERAL', 1800.00, 1500.00, 365, 1,
   '[{"name":"五健筛查","count":1,"description":"血压/血糖/血脂/骨密度/认知功能筛查"},{"name":"报告解读","count":1,"description":"筛查结果电话解读"},{"name":"健康建议","count":2,"description":"年度健康管理建议"}]',
   'ON_SHELF', 5);

-- -----------------------------------------------------------
-- 演示订单
-- -----------------------------------------------------------
INSERT INTO `package_orders` (`id`, `order_no`, `user_id`, `family_id`, `package_id`, `amount`, `original_amount`, `payment_method`, `status`, `start_date`, `end_date`, `paid_at`) VALUES
(1001, 'HF2026011500001', 100, 100, 1, 9800.00, 12800.00, 'WECHAT_PAY', 'ACTIVE', '2026-01-15', '2027-01-14', '2026-01-15 10:30:00'),
(1002, 'HF2026012000002', 101, 101, 2, 18800.00, 22800.00, 'WECHAT_PAY', 'ACTIVE', '2026-01-20', '2027-01-19', '2026-01-20 14:20:00');

-- -----------------------------------------------------------
-- 演示健康档案（时光轴事件）
-- -----------------------------------------------------------
INSERT INTO `health_records` (`id`, `user_id`, `record_type`, `event_date`, `gestational_week`, `event_title`, `event_summary`, `source`, `hospital_dept`, `attending_doctor`, `abnormal_flag`) VALUES
(2001, 100, 'CHECKUP',    '2025-11-01', '8w0d',  '首次产检 · 确认妊娠', 'B超可见胎心胎芽，孕酮正常', 'HIS', '产科', '李主任', 0),
(2002, 100, 'REPORT',     '2025-12-01', '12w3d', 'NT检查', 'NT值1.2mm，正常范围', 'PACS', '超声科', '王医生', 0),
(2003, 100, 'REPORT',     '2026-01-05', '17w0d', '唐氏筛查', '21三体风险1:1200，低风险', 'LIS', '检验科', NULL, 0),
(2004, 100, 'CHECKUP',    '2026-01-18', '19w1d', '常规产检', '血压120/80，体重增长正常', 'HIS', '产科', '李主任', 0),
(2005, 100, 'REPORT',     '2026-01-20', '19w3d', '糖耐量筛查（OGTT）', '空腹5.1mmol/L↑ 临界值', 'LIS', '检验科', NULL, 1),
(2006, 100, 'SELF_RECORD','2026-01-21', '19w4d', '居家自测血糖', '餐后2h血糖6.8mmol/L', 'DEVICE', NULL, NULL, 0),
(2007, 100, 'MILESTONE',  '2026-01-15', '18w5d', '首次胎动', '感受到宝宝第一次胎动', 'MANUAL', NULL, NULL, 0);

INSERT INTO `health_records` (`id`, `user_id`, `record_type`, `event_date`, `event_title`, `event_summary`, `source`, `hospital_dept`, `attending_doctor`, `abnormal_flag`) VALUES
(2008, 101, 'CHECKUP',    '2025-10-15', '确认妊娠', 'B超确认宫内早孕', 'HIS', '产科', '李主任', 0),
(2009, 101, 'REPORT',     '2025-11-15', 'NT检查', 'NT正常', 'PACS', '超声科', '王医生', 0),
(2010, 101, 'DELIVERY',   '2026-01-10', '剖宫产分娩', '孕39周剖宫产，男婴，体重3400g', 'EMR', '产科', '李主任', 0),
(2011, 101, 'CHECKUP',    '2026-02-20', '产后42天复查', '子宫恢复良好，伤口愈合正常', 'HIS', '产科', '李主任', 0);

-- -----------------------------------------------------------
-- 演示检验指标
-- -----------------------------------------------------------
INSERT INTO `lab_reports` (`id`, `health_record_id`, `report_type`, `indicator_name`, `indicator_code`, `result_value`, `unit`, `reference_range`, `is_abnormal`, `report_date`) VALUES
(3001, 2003, 'LAB', '21三体风险值', 'DS_RISK', '1:1200', NULL, '>1:270', 0, '2026-01-05'),
(3002, 2003, 'LAB', '18三体风险值', 'ES_RISK', '1:50000', NULL, '>1:350', 0, '2026-01-05'),
(3003, 2005, 'LAB', '空腹血糖', 'GLU', '5.1', 'mmol/L', '3.9-5.1', 1, '2026-01-20'),
(3004, 2005, 'LAB', '餐后1h血糖', 'GLU_1H', '10.0', 'mmol/L', '<10.0', 1, '2026-01-20'),
(3005, 2005, 'LAB', '餐后2h血糖', 'GLU_2H', '8.5', 'mmol/L', '<8.5', 1, '2026-01-20'),
(3006, 2004, 'LAB', '血红蛋白', 'HGB', '115', 'g/L', '110-160', 0, '2026-01-18'),
(3007, 2004, 'LAB', '收缩压', 'SBP', '120', 'mmHg', '<140', 0, '2026-01-18'),
(3008, 2004, 'LAB', '舒张压', 'DBP', '80', 'mmHg', '<90', 0, '2026-01-18');

-- -----------------------------------------------------------
-- 演示随访任务
-- -----------------------------------------------------------
INSERT INTO `followup_tasks` (`id`, `user_id`, `family_id`, `health_record_id`, `task_type`, `trigger_condition`, `scheduled_date`, `priority`, `status`, `assigned_butler_id`, `followup_method`) VALUES
(4001, 100, 100, 2005, 'ABNORMAL_LAB', '糖耐量筛查空腹血糖临界值', '2026-01-22', 'HIGH', 'COMPLETED', 10, 'PHONE'),
(4002, 100, 100, NULL, 'GESTATIONAL', '孕20周常规随访', '2026-01-25', 'NORMAL', 'PENDING', 10, 'WECOM'),
(4003, 101, 101, 2011, 'POST_DISCHARGE', '产后42天复查随访', '2026-02-22', 'NORMAL', 'ASSIGNED', 11, 'PHONE'),
(4004, 100, 100, NULL, 'GESTATIONAL', '孕24周常规随访', '2026-02-22', 'NORMAL', 'PENDING', NULL, 'PHONE');

-- -----------------------------------------------------------
-- 演示仪表盘缓存数据
-- -----------------------------------------------------------
INSERT INTO `dashboard_cache` (`metric_name`, `metric_value`, `metric_label`, `dimension`, `dimension_value`, `period_start`, `period_end`, `delta_percent`) VALUES
('active_members',     2847,   '活跃会员',     'MONTHLY', '2026-01', '2026-01-01', '2026-01-31', 12.0),
('monthly_revenue',    486200, '本月营收',     'MONTHLY', '2026-01', '2026-01-01', '2026-01-31', 8.5),
('followup_rate',      94.3,   '随访完成率',   'MONTHLY', '2026-01', '2026-01-01', '2026-01-31', 2.1),
('satisfaction_score', 4.82,   '满意度评分',   'MONTHLY', '2026-01', '2026-01-01', '2026-01-31', 3.2),
('sales_rank',         273,    '产后康复套餐销量','MONTHLY','2026-01', '2026-01-01', '2026-01-31', 15.0),
('conversion_rate',    23.5,   '注册→购买转化率','MONTHLY','2026-01', '2026-01-01', '2026-01-31', 1.8);
