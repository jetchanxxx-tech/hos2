package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "package_orders")
  public class PackageOrder extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "package_snapshot_json", columnDefinition = "JSON")
    private String packageSnapshotJson;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    @Column(name = "payment_trade_no", length = 128)
    private String paymentTradeNo;

    @Column(name = "status", nullable = false, length = 16)
        private String status = "PENDING";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    @Column(name = "remark", length = 512)
    private String remark;

    public PackageOrder() {}

    // ---- Getters & Setters ----
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public String getPackageSnapshotJson() { return packageSnapshotJson; }
    public void setPackageSnapshotJson(String packageSnapshotJson) { this.packageSnapshotJson = packageSnapshotJson; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentTradeNo() { return paymentTradeNo; }
    public void setPaymentTradeNo(String paymentTradeNo) { this.paymentTradeNo = paymentTradeNo; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public LocalDateTime getRefundAt() { return refundAt; }
    public void setRefundAt(LocalDateTime refundAt) { this.refundAt = refundAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
