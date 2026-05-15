package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.FollowupTask;
import com.huifu.starchain.service.FollowupService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/followups")

public class FollowupController {

    private final FollowupService followupService;

    public FollowupController(FollowupService followupService) { this.followupService = followupService; }

    @GetMapping("/butler")
    public ApiResponse<PageResult<FollowupTask>> butlerTasks(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(followupService.getButlerTasks(userId, status, page, size));
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<FollowupTask>> myTasks(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(followupService.getMyTasks(userId, page, size));
    }

    @PostMapping
    public ApiResponse<FollowupTask> create(@RequestBody FollowupTask task) {
        return ApiResponse.ok(followupService.createManualTask(task));
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<FollowupTask> assign(@PathVariable Long id, @RequestParam Long butlerId) {
        return ApiResponse.ok(followupService.assignTask(id, butlerId));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<FollowupTask> complete(@PathVariable Long id,
                                               @AuthenticationPrincipal Long userId,
                                               @RequestParam(required = false) String note,
                                               @RequestBody(required = false) String completionJson) {
        return ApiResponse.ok(followupService.completeTask(id, userId, note, completionJson));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(followupService.getTaskStats());
    }

    @GetMapping("/overdue")
    public ApiResponse<?> overdue() {
        return ApiResponse.ok(followupService.getOverdueTasks());
    }
}
