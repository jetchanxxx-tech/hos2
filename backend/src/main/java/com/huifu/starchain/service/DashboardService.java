package com.huifu.starchain.service;

import com.huifu.starchain.entity.DashboardCache;
import com.huifu.starchain.entity.User;
import com.huifu.starchain.repository.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service

public class DashboardService {

    private final DashboardCacheRepository cacheRepo;
    private final UserRepository userRepo;
    private final PackageOrderRepository orderRepo;
    private final FollowupTaskRepository followupRepo;

    public DashboardService(DashboardCacheRepository cacheRepo, UserRepository userRepo, PackageOrderRepository orderRepo, FollowupTaskRepository followupRepo) { this.cacheRepo = cacheRepo; this.userRepo = userRepo; this.orderRepo = orderRepo; this.followupRepo = followupRepo; }

    public Map<String, Object> getKpiSummary() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDateTime since = monthStart.atStartOfDay();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("activeMembers", userRepo.countByStatus(User.UserStatus.ACTIVE));
        kpis.put("newMembersThisMonth", userRepo.countNewResidentsSince(since));
        kpis.put("familiesCount", userRepo.countWithFamily());
        kpis.put("monthlyRevenue", orderRepo.sumRevenueSince(since));
        kpis.put("followupCompleted", followupRepo.countCompletedSince(since));
        kpis.put("satisfactionScore", BigDecimal.valueOf(4.82));

        // Calculate follow-up rate
        long scheduled = followupRepo.countScheduledBetween(monthStart, LocalDate.now());
        long completed = followupRepo.countCompletedSince(since);
        double rate = scheduled > 0 ? (double) completed / scheduled * 100 : 0;
        kpis.put("followupRate", Math.round(rate * 10) / 10.0);

        return kpis;
    }

    public List<Map<String, Object>> getSalesRanking() {
        List<Object[]> rows = orderRepo.salesRankingSince(LocalDate.now().minusMonths(1).atStartOfDay());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("packageId", row[0]);
            entry.put("count", row[1]);
            entry.put("revenue", row[2]);
            result.add(entry);
        }
        return result;
    }

    public List<DashboardCache> getMetricHistory(String metricName, LocalDate from, LocalDate to) {
        return cacheRepo.findByMetricNameAndDimensionAndPeriodStartBetweenOrderByCalculatedAtAsc(
                metricName, "MONTHLY", from, to);
    }

    public List<Map<String, Object>> getRecentActivity() {
        // In production: query from a unified activity stream
        // For MVP, return mock activity feed
        List<Map<String, Object>> activities = new ArrayList<>();
        activities.add(Map.of("type", "followup", "actor", "李医生", "action", "完成产后42天随访",
                "target", "张*芳", "time", "2 分钟前"));
        activities.add(Map.of("type", "redemption", "actor", "王莉", "action", "核销 VIP 陪诊服务",
                "target", "陈*婷", "time", "8 分钟前"));
        activities.add(Map.of("type", "system", "actor", "系统", "action", "自动同步 14 份检验报告",
                "target", "惠福时光轴", "time", "15 分钟前"));
        activities.add(Map.of("type", "task", "actor", "刘护士", "action", "创建疫苗提醒任务",
                "target", "王*娟", "time", "32 分钟前"));
        activities.add(Map.of("type", "alert", "actor", "惠福灵犀", "action", "触发紧急预警",
                "target", "关键词\"出血\"", "time", "48 分钟前"));
        return activities;
    }

    public Map<String, Object> getButlerLeaderboard() {
        Map<String, Object> board = new LinkedHashMap<>();
        board.put("topButlers", List.of(
                Map.of("name", "陈*华", "points", 1250, "rank", 1),
                Map.of("name", "刘*霞", "points", 980, "rank", 2),
                Map.of("name", "黄*强", "points", 870, "rank", 3)));
        return board;
    }
}
