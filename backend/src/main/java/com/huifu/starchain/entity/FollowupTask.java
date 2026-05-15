package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "followup_tasks")
  public class FollowupTask extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "health_record_id")
    private Long healthRecordId;

    @Column(name = "task_type", nullable = false, length = 32)
    private String taskType;

    @Column(name = "trigger_condition", length = 256)
    private String triggerCondition;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "priority", nullable = false, length = 8)
        private String priority = "NORMAL";

    @Column(name = "status", nullable = false, length = 16)
        private String status = "PENDING";

    @Column(name = "assigned_butler_id")
    private Long assignedButlerId;

    @Column(name = "followup_method", nullable = false, length = 16)
        private String followupMethod = "PHONE";

    @Column(name = "template_id", length = 64)
    private String templateId;

    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;

    @Column(name = "completion_json", columnDefinition = "JSON")
    private String completionJson;

    @Column(name = "ai_generated")
        private Boolean aiGenerated = false;

    @Column(name = "ai_call_id", length = 128)
    private String aiCallId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by")
    private Long completedBy;

    public FollowupTask() {}

    // ---- Getters & Setters ----
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public Long getHealthRecordId() { return healthRecordId; }
    public void setHealthRecordId(Long healthRecordId) { this.healthRecordId = healthRecordId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public LocalDate getDeadlineDate() { return deadlineDate; }
    public void setDeadlineDate(LocalDate deadlineDate) { this.deadlineDate = deadlineDate; }
    public Long getAssignedButlerId() { return assignedButlerId; }
    public void setAssignedButlerId(Long assignedButlerId) { this.assignedButlerId = assignedButlerId; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getCompletionNote() { return completionNote; }
    public void setCompletionNote(String completionNote) { this.completionNote = completionNote; }
    public String getCompletionJson() { return completionJson; }
    public void setCompletionJson(String completionJson) { this.completionJson = completionJson; }
    public String getAiCallId() { return aiCallId; }
    public void setAiCallId(String aiCallId) { this.aiCallId = aiCallId; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Long getCompletedBy() { return completedBy; }
    public void setCompletedBy(Long completedBy) { this.completedBy = completedBy; }
}
