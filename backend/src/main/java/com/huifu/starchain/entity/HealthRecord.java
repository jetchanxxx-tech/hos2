package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "health_records")
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
        private String source = "MANUAL";

    @Column(name = "source_ref_id", length = 128)
    private String sourceRefId;

    @Column(name = "hospital_dept", length = 128)
    private String hospitalDept;

    @Column(name = "attending_doctor", length = 64)
    private String attendingDoctor;

    @Column(name = "abnormal_flag")
        private Integer abnormalFlag = 0;

    @Column(name = "alert_triggered")
        private Boolean alertTriggered = false;

    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;

    public HealthRecord() {}

    // ---- Getters & Setters ----
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
    public String getGestationalWeek() { return gestationalWeek; }
    public void setGestationalWeek(String gestationalWeek) { this.gestationalWeek = gestationalWeek; }
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getEventSummary() { return eventSummary; }
    public void setEventSummary(String eventSummary) { this.eventSummary = eventSummary; }
    public String getDetailJson() { return detailJson; }
    public void setDetailJson(String detailJson) { this.detailJson = detailJson; }
    public String getSourceRefId() { return sourceRefId; }
    public void setSourceRefId(String sourceRefId) { this.sourceRefId = sourceRefId; }
    public String getHospitalDept() { return hospitalDept; }
    public void setHospitalDept(String hospitalDept) { this.hospitalDept = hospitalDept; }
    public String getAttendingDoctor() { return attendingDoctor; }
    public void setAttendingDoctor(String attendingDoctor) { this.attendingDoctor = attendingDoctor; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Boolean getAlertTriggered() { return alertTriggered; }
    public void setAlertTriggered(Boolean alertTriggered) { this.alertTriggered = alertTriggered; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public Integer getAbnormalFlag() { return abnormalFlag; }
    public void setAbnormalFlag(Integer abnormalFlag) { this.abnormalFlag = abnormalFlag; }
    public String getTagsJson() { return tagsJson; }
    public void setTagsJson(String tagsJson) { this.tagsJson = tagsJson; }

    @Column(name = "is_deleted")
        private Boolean isDeleted = false;
}
