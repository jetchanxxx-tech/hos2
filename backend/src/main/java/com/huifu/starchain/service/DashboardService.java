package com.huifu.starchain.service;

import com.huifu.starchain.entity.DashboardCache;
import com.huifu.starchain.entity.User;
import com.huifu.starchain.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service

public class DashboardService {

    private final DashboardCacheRepository cacheRepo;
    private final UserRepository userRepo;
    private final PackageOrderRepository orderRepo;
    private final FollowupTaskRepository followupRepo;
    private final ChatSessionRepository chatSessionRepo;
    private final AuditLogRepository auditLogRepo;
    private final ServicePackageRepository pkgRepo;

    public DashboardService(DashboardCacheRepository cacheRepo, UserRepository userRepo,
            PackageOrderRepository orderRepo, FollowupTaskRepository followupRepo,
            ChatSessionRepository chatSessionRepo, AuditLogRepository auditLogRepo,
            ServicePackageRepository pkgRepo) {
        this.cacheRepo = cacheRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.followupRepo = followupRepo;
        this.chatSessionRepo = chatSessionRepo;
        this.auditLogRepo = auditLogRepo;
        this.pkgRepo = pkgRepo;
    }

    public Map<String, Object> getKpiSummary() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDateTime since = monthStart.atStartOfDay();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("activeMembers", userRepo.countByStatus(User.UserStatus.ACTIVE));
        kpis.put("newMembersThisMonth", userRepo.countNewResidentsSince(since));
        kpis.put("familiesCount", userRepo.countWithFamily());
        kpis.put("monthlyRevenue", orderRepo.sumRevenueSince(since));
        kpis.put("followupCompleted", followupRepo.countCompletedSince(since));

        // 真实满意度
        Double avgSat = chatSessionRepo.avgSatisfaction();
        kpis.put("satisfactionScore", BigDecimal.valueOf(avgSat != null ? avgSat : 0).setScale(2, RoundingMode.HALF_UP));

        // 随访完成率
        long scheduled = followupRepo.countScheduledBetween(monthStart, LocalDate.now());
        long completed = followupRepo.countCompletedSince(since);
        double rate = scheduled > 0 ? (double) completed / scheduled * 100 : 0;
        kpis.put("followupRate", Math.round(rate * 10) / 10.0);

        // 订单数量
        kpis.put("packageOrderCount", orderRepo.count());
        return kpis;
    }

    public List<Map<String, Object>> getSalesRanking() {
        List<Object[]> rows = orderRepo.salesRankingSince(LocalDate.now().minusMonths(1).atStartOfDay());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            Long packageId = (Long) row[0];
            entry.put("packageId", packageId);
            entry.put("count", row[1]);
            entry.put("revenue", row[2]);
            // 回填服务包名称和价格
            pkgRepo.findById(packageId).ifPresent(pkg -> {
                entry.put("packageName", pkg.getName());
                entry.put("price", pkg.getPrice());
                entry.put("status", pkg.getStatus().name());
            });
            result.add(entry);
        }
        return result;
    }

    public List<DashboardCache> getMetricHistory(String metricName, LocalDate from, LocalDate to) {
        return cacheRepo.findByMetricNameAndDimensionAndPeriodStartBetweenOrderByCalculatedAtAsc(
                metricName, "MONTHLY", from, to);
    }

    public List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();
        var logs = auditLogRepo.findRecent(PageRequest.of(0, 20));
        for (var log : logs.getContent()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("action", log.getAction());
            entry.put("resourceType", log.getResourceType());
            entry.put("time", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
            entry.put("result", log.getResult());
            entry.put("userId", log.getUserId());
            activities.add(entry);
        }
        return activities;
    }

    public Map<String, Object> getButlerLeaderboard() {
        Map<String, Object> board = new LinkedHashMap<>();
        List<Object[]> rows = followupRepo.butlerCompletionRanking();
        List<Map<String, Object>> topButlers = new ArrayList<>();
        int rank = 0;
        for (Object[] row : rows) {
            if (rank >= 10) break;
            rank++;
            Long butlerId = (Long) row[0];
            Long count = (Long) row[1];
            String name = "管家#" + butlerId;
            try {
                var u = userRepo.findById(butlerId);
                if (u.isPresent()) name = u.get().getNameMasked();
            } catch (Exception ignored) {}
            topButlers.add(Map.of("name", name, "followupCount", count, "points", count * 5, "rank", rank));
        }
        board.put("topButlers", topButlers);
        return board;
    }

    /** 满意度分布（1-5 分各数量） */
    public List<Map<String, Object>> getSatisfactionDistribution() {
        List<Object[]> rows = chatSessionRepo.satisfactionDistribution();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(Map.of("score", row[0], "count", row[1]));
        }
        return result;
    }

    /** 随访按管家分组的完成率 */
    public List<Map<String, Object>> getFollowupByButler() {
        List<Object[]> rows = followupRepo.butlerCompletionRanking();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long butlerId = (Long) row[0];
            Long completed = (Long) row[1];
            String name = "管家#" + butlerId;
            try {
                var u = userRepo.findById(butlerId);
                if (u.isPresent()) name = u.get().getNameMasked();
            } catch (Exception ignored) {}
            result.add(Map.of("butlerName", name, "completedTasks", completed));
        }
        return result;
    }
}
