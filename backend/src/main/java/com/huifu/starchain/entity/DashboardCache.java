package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_cache")
@Getter @NoArgsConstructor public class DashboardCache extends BaseEntity {

    @Column(name = "metric_name", nullable = false, length = 64)
    private String metricName;

    @Column(name = "metric_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "metric_label", length = 128)
    private String metricLabel;

    @Column(name = "dimension", nullable = false, length = 32)
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

    public DashboardCache() {}

    // ---- Getters & Setters ----
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
    public String getMetricLabel() { return metricLabel; }
    public void setMetricLabel(String metricLabel) { this.metricLabel = metricLabel; }
    public String getDimensionValue() { return dimensionValue; }
    public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getDeltaValue() { return deltaValue; }
    public void setDeltaValue(BigDecimal deltaValue) { this.deltaValue = deltaValue; }
    public BigDecimal getDeltaPercent() { return deltaPercent; }
    public void setDeltaPercent(BigDecimal deltaPercent) { this.deltaPercent = deltaPercent; }
    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }

    @Column(name = "calculated_at", nullable = false)
        private LocalDateTime calculatedAt = LocalDateTime.now();
}
