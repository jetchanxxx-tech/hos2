package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.entity.DashboardCache;
import com.huifu.starchain.service.DashboardService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")

public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi-summary")
    public ApiResponse<Map<String, Object>> kpiSummary() {
        return ApiResponse.ok(dashboardService.getKpiSummary());
    }

    @GetMapping("/sales-ranking")
    public ApiResponse<List<Map<String, Object>>> salesRanking() {
        return ApiResponse.ok(dashboardService.getSalesRanking());
    }

    @GetMapping("/metrics/{metricName}/history")
    public ApiResponse<List<DashboardCache>> metricHistory(
            @PathVariable String metricName,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate f = from != null ? from : LocalDate.now().minusMonths(6);
        LocalDate t = to != null ? to : LocalDate.now();
        return ApiResponse.ok(dashboardService.getMetricHistory(metricName, f, t));
    }

    @GetMapping("/activity-feed")
    public ApiResponse<List<Map<String, Object>>> activityFeed() {
        return ApiResponse.ok(dashboardService.getRecentActivity());
    }

    @GetMapping("/butler-leaderboard")
    public ApiResponse<Map<String, Object>> butlerLeaderboard() {
        return ApiResponse.ok(dashboardService.getButlerLeaderboard());
    }
}
