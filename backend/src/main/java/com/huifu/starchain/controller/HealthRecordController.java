package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.HealthRecord;
import com.huifu.starchain.entity.LabReport;
import com.huifu.starchain.service.HealthRecordService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/records")

public class HealthRecordController {

    private final HealthRecordService recordService;

    public HealthRecordController(HealthRecordService recordService) { this.recordService = recordService; }

    @GetMapping
    public ApiResponse<PageResult<HealthRecord>> getTimeline(
            @AuthenticationPrincipal Long requesterUserId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String recordType) {
        return ApiResponse.ok(recordService.getTimeline(requesterUserId, userId, page, size, recordType));
    }

    @GetMapping("/range")
    public ApiResponse<List<HealthRecord>> getTimelineByRange(
            @AuthenticationPrincipal Long requesterUserId,
            @RequestParam Long userId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ApiResponse.ok(recordService.getTimelineByDateRange(requesterUserId, userId, from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthRecord> getRecord(@PathVariable Long id) {
        return ApiResponse.ok(recordService.getRecord(id));
    }

    @PostMapping
    public ApiResponse<HealthRecord> createRecord(@RequestBody HealthRecord record) {
        return ApiResponse.ok(recordService.createRecord(record));
    }

    @GetMapping("/abnormal")
    public ApiResponse<List<HealthRecord>> getAbnormal(
            @AuthenticationPrincipal Long requesterUserId,
            @RequestParam Long userId) {
        return ApiResponse.ok(recordService.getAbnormalRecords(requesterUserId, userId));
    }

    @GetMapping("/{id}/reports")
    public ApiResponse<List<LabReport>> getReports(
            @AuthenticationPrincipal Long requesterUserId,
            @PathVariable Long id) {
        return ApiResponse.ok(recordService.getReportsByRecord(requesterUserId, id));
    }

    @GetMapping("/trends/{indicatorCode}")
    public ApiResponse<List<LabReport>> getTrend(@PathVariable String indicatorCode) {
        return ApiResponse.ok(recordService.getTrend(indicatorCode));
    }

    @GetMapping("/source-stats")
    public ApiResponse<Map<String, Long>> getSourceStats(@RequestParam(defaultValue = "2026-01-01") LocalDate since) {
        return ApiResponse.ok(recordService.getSourceStats(since));
    }
}
