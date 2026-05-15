package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "health_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HealthRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_type", nullable = false, length = 32)
    private String recordType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "gestational_week", length = 8)
    private String gestationalWeek;

    @Column(name = "event_title", nullable = false, length = 256)
    private String eventTitle;

    @Column(name = "event_summary", columnDefinition = "TEXT")
    private String eventSummary;

    @Column(name = "detail_json", columnDefinition = "JSON")
    private String detailJson;

    @Column(name = "source", nullable = false, length = 16)
    @Builder.Default
    private String source = "MANUAL";

    @Column(name = "source_ref_id", length = 128)
    private String sourceRefId;

    @Column(name = "hospital_dept", length = 128)
    private String hospitalDept;

    @Column(name = "attending_doctor", length = 64)
    private String attendingDoctor;

    @Column(name = "abnormal_flag")
    @Builder.Default
    private Integer abnormalFlag = 0;

    @Column(name = "alert_triggered")
    @Builder.Default
    private Boolean alertTriggered = false;

    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
