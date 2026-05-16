package com.huifu.starchain.service;

import com.huifu.starchain.entity.DashboardCache;
import com.huifu.starchain.repository.DashboardCacheRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 仪表盘缓存同步 — 每天凌晨 1:00 将 KPI 快照写入 dashboard_cache 表
 * 供历史趋势查询
 */
@Service
public class DashboardCacheSync {

    private static final Logger log = LoggerFactory.getLogger(DashboardCacheSync.class);

    private final DashboardCacheRepository cacheRepo;
    private final DashboardService dashboardService;

    public DashboardCacheSync(DashboardCacheRepository cacheRepo, DashboardService dashboardService) {
        this.cacheRepo = cacheRepo;
        this.dashboardService = dashboardService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void syncDaily() {
        try {
            var kpis = dashboardService.getKpiSummary();
            LocalDate today = LocalDate.now();
            syncMetric("ACTIVE_MEMBERS", String.valueOf(kpis.get("activeMembers")), today);
            syncMetric("MONTHLY_REVENUE", String.valueOf(kpis.get("monthlyRevenue")), today);
            syncMetric("FOLLOWUP_RATE", String.valueOf(kpis.get("followupRate")), today);
            syncMetric("SATISFACTION", String.valueOf(kpis.get("satisfactionScore")), today);
            syncMetric("PACKAGE_ORDERS", String.valueOf(kpis.get("packageOrderCount")), today);
            log.info("Dashboard cache synced for {}", today);
        } catch (Exception e) {
            log.error("Dashboard cache sync failed", e);
        }
    }

    private void syncMetric(String name, String value, LocalDate date) {
        DashboardCache cache = new DashboardCache();
        cache.setMetricName(name);
        cache.setMetricValue(new BigDecimal(value));
        cache.setPeriodStart(date);
        cache.setPeriodEnd(date);
        cache.setDimension("DAILY");
        cache.setCalculatedAt(LocalDateTime.now());
        cacheRepo.save(cache);
    }
}
