package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lab_reports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LabReport extends BaseEntity {

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
    @Builder.Default
    private Integer isAbnormal = 0;

    @Column(name = "abnormal_direction", length = 8)
    private String abnormalDirection;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "machine_info", length = 256)
    private String machineInfo;

    @Column(name = "trend_data_json", columnDefinition = "JSON")
    private String trendDataJson;
}
