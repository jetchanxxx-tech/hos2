package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_cache")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DashboardCache extends BaseEntity {

    @Column(name = "metric_name", nullable = false, length = 64)
    private String metricName;

    @Column(name = "metric_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "metric_label", length = 128)
    private String metricLabel;

    @Column(name = "dimension", nullable = false, length = 32)
    @Builder.Default
    private String dimension = "OVERALL";

    @Column(name = "dimension_value", length = 128)
    private String dimensionValue;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "delta_value", precision = 18, scale = 4)
    private BigDecimal deltaValue;

    @Column(name = "delta_percent", precision = 8, scale = 4)
    private BigDecimal deltaPercent;

    @Column(name = "meta_json", columnDefinition = "JSON")
    private String metaJson;

    @Column(name = "calculated_at", nullable = false)
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
