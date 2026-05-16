package com.huifu.starchain.service;

import com.huifu.starchain.entity.*;
import com.huifu.starchain.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 随访规则引擎 — 按预定义规则自动生成随访任务
 * - 异常检验指标触发随访（优先级 HIGH）
 * - 孕周关键节点自动随访（NT/糖耐/大排畸/37W 产前）
 * - 产后 42 天未复查提醒
 */
@Service
public class FollowupRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(FollowupRuleEngine.class);

    private final FollowupTaskRepository taskRepo;
    private final HealthRecordRepository recordRepo;
    private final LabReportRepository labRepo;
    private final UserRepository userRepo;

    public FollowupRuleEngine(FollowupTaskRepository taskRepo, HealthRecordRepository recordRepo,
            LabReportRepository labRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo; this.recordRepo = recordRepo;
        this.labRepo = labRepo; this.userRepo = userRepo;
    }

    /**
     * 每 30 分钟扫描一次，为异常指标自动创建随访任务
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void scanAbnormalReports() {
        var abnormalReports = labRepo.findByIsAbnormalAndReportDateAfter(1, LocalDate.now().minusDays(7));
        for (var r : abnormalReports) {
            boolean exists = taskRepo.findByTriggerCondition("lab_abnormal:" + r.getIndicatorCode()).stream()
                    .anyMatch(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1)));
            if (exists) continue;

            FollowupTask task = new FollowupTask();
            task.setUserId(recordRepo.findById(r.getHealthRecordId()).map(HealthRecord::getUserId).orElse(null));
            task.setTaskType("异常指标随访：" + r.getIndicatorName());
            task.setCompletionNote("指标 " + r.getIndicatorName() + " 异常（值=" + r.getResultValue() + r.getUnit()
                    + "，参考 " + r.getReferenceRange() + "），请电话随访。");
            task.setScheduledDate(LocalDate.now().plusDays(1));
            task.setStatus("PENDING");
            task.setPriority("HIGH");
            task.setFollowupMethod("PHONE");
            task.setTriggerCondition("lab_abnormal:" + r.getIndicatorCode());
            taskRepo.save(task);
            log.info("创建异常指标随访任务 #{}: {}", task.getId(), r.getIndicatorCode());
        }
    }

    /**
     * 每天凌晨 2:00 扫描孕周关键节点，创建产检提醒随访
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void scanPregnancyMilestones() {
        // 查找所有 RESIDENT 角色的活跃用户
        var residents = userRepo.findByRole(User.UserRole.RESIDENT,
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        for (var u : residents) {
            // 查找最新的 CHECKUP 或 PREGNANCY_PREP 记录
            var latestRecord = recordRepo.findTopByUserIdAndRecordTypeInOrderByEventDateDesc(
                    u.getId(), List.of("CHECKUP", "PREGNANCY_PREP", "DELIVERY", "POSTPARTUM"));
            if (latestRecord.isEmpty()) continue;

            var r = latestRecord.get();
            String gw = r.getGestationalWeek();
            if (gw == null || gw.isEmpty()) continue;

            try {
                int week = Integer.parseInt(gw.replaceAll("[^0-9]", ""));
                // 关键节点：11W(NT), 20W(大排畸), 24W(糖耐), 34W(胎监), 37W(产前)
                int[] milestones = {11, 20, 24, 34, 37};
                String[] milestoneNames = {"NT检查", "大排畸B超", "糖耐量试验", "胎心监护", "产前评估"};
                for (int i = 0; i < milestones.length; i++) {
                    if (week == milestones[i]) {
                        createMilestoneTask(u.getId(), milestoneNames[i] + " (孕" + week + "周)", milestoneNames[i]);
                    }
                }
                // 产后42天未复查
                if ("POSTPARTUM".equals(r.getRecordType())) {
                    long daysSince = java.time.temporal.ChronoUnit.DAYS.between(r.getEventDate(), LocalDate.now());
                    if (daysSince >= 42) {
                        createMilestoneTaskIfNotExists(u.getId(), "产后42天复查提醒", "请提醒用户来院复查");
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void createMilestoneTask(Long userId, String name, String desc) {
        createMilestoneTaskIfNotExists(userId, name, desc);
    }

    private void createMilestoneTaskIfNotExists(Long userId, String name, String desc) {
        boolean exists = taskRepo.findByTaskTypeAndUserId(name, userId).stream()
                .anyMatch(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)));
        if (!exists) {
            FollowupTask task = new FollowupTask();
            task.setUserId(userId);
            task.setTaskType(name);
            task.setCompletionNote(desc);
            task.setScheduledDate(LocalDate.now());
            task.setStatus("PENDING");
            task.setPriority("NORMAL");
            task.setFollowupMethod("WECHAT");
            task.setTriggerCondition("time:" + name.replaceAll("\\s+", "_"));
            taskRepo.save(task);
            log.info("Auto-created milestone followup: {} for user {}", name, userId);
        }
    }
}
