package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "followup_tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
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
    @Builder.Default
    private String priority = "NORMAL";

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "assigned_butler_id")
    private Long assignedButlerId;

    @Column(name = "followup_method", nullable = false, length = 16)
    @Builder.Default
    private String followupMethod = "PHONE";

    @Column(name = "template_id", length = 64)
    private String templateId;

    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;

    @Column(name = "completion_json", columnDefinition = "JSON")
    private String completionJson;

    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "ai_call_id", length = 128)
    private String aiCallId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by")
    private Long completedBy;
}
