package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.FollowupTask;
import com.huifu.starchain.repository.FollowupTaskRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class FollowupService {

    private final FollowupTaskRepository taskRepo;

    public PageResult<FollowupTask> getButlerTasks(Long butlerId, String status, int page, int size) {
        var pg = taskRepo.findByAssignedButlerIdAndStatusOrderByScheduledDateAsc(
                butlerId, status != null ? status : "PENDING", PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public PageResult<FollowupTask> getMyTasks(Long userId, int page, int size) {
        var pg = taskRepo.findByUserIdOrderByScheduledDateDesc(userId, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public List<FollowupTask> getOverdueTasks() {
        return taskRepo.findByStatusAndScheduledDateBefore("PENDING", LocalDate.now());
    }

    @Transactional
    public FollowupTask completeTask(Long taskId, Long completedBy, String note, String completionJson) {
        FollowupTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new BusinessException(BizError.TASK_NOT_FOUND));
        if ("COMPLETED".equals(task.getStatus())) {
            throw new BusinessException(BizError.TASK_ALREADY_DONE);
        }
        task.setStatus("COMPLETED");
        task.setCompletionNote(note);
        task.setCompletionJson(completionJson);
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletedBy(completedBy);
        return taskRepo.save(task);
    }

    @Transactional
    public FollowupTask createManualTask(FollowupTask task) {
        return taskRepo.save(task);
    }

    @Transactional
    public FollowupTask assignTask(Long taskId, Long butlerId) {
        FollowupTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new BusinessException(BizError.TASK_NOT_FOUND));
        task.setAssignedButlerId(butlerId);
        task.setStatus("ASSIGNED");
        return taskRepo.save(task);
    }

    public Map<String, Long> getTaskStats() {
        return taskRepo.countByStatus().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    public long getCompletedCount(LocalDate since) {
        return taskRepo.countCompletedSince(since.atStartOfDay());
    }

    public long getScheduledCount(LocalDate from, LocalDate to) {
        return taskRepo.countScheduledBetween(from, to);
    }
}
