package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "lab_reports")
@Getter @NoArgsConstructor public class LabReport extends BaseEntity {

    @Column(name = "health_record_id", nullable = false)
    private Long healthRecordId;

    @Column(name = "report_type", nullable = false, length = 32)
    private String reportType;

    @Column(name = "indicator_name", nullable = false, length = 128)
    private String indicatorName;

    @Column(name = "indicator_code", length = 64)
    private String indicatorCode;

    @Column(name = "result_value", length = 128)
    private String resultValue;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "reference_range", length = 128)
    private String referenceRange;

    @Column(name = "is_abnormal")
        private Integer isAbnormal = 0;

    @Column(name = "abnormal_direction", length = 8)
    private String abnormalDirection;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "machine_info", length = 256)
    private String machineInfo;

    @Column(name = "trend_data_json", columnDefinition = "JSON")
    private String trendDataJson;

    public LabReport() {}

    // ---- Getters & Setters ----
    public Long getHealthRecordId() { return healthRecordId; }
    public void setHealthRecordId(Long healthRecordId) { this.healthRecordId = healthRecordId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
    public String getIndicatorCode() { return indicatorCode; }
    public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public String getAbnormalDirection() { return abnormalDirection; }
    public void setAbnormalDirection(String abnormalDirection) { this.abnormalDirection = abnormalDirection; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getMachineInfo() { return machineInfo; }
    public void setMachineInfo(String machineInfo) { this.machineInfo = machineInfo; }
    public String getTrendDataJson() { return trendDataJson; }
    public void setTrendDataJson(String trendDataJson) { this.trendDataJson = trendDataJson; }
}
