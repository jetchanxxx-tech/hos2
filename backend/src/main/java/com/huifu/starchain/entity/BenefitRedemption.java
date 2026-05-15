package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "benefit_redemptions")
@Getter @NoArgsConstructor public class BenefitRedemption extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "benefit_type", nullable = false, length = 32)
    private String benefitType;

    @Column(name = "benefit_name", nullable = false, length = 128)
    private String benefitName;

    @Column(name = "total_count")
        private Integer totalCount = 1;

    @Column(name = "used_count")
        private Integer usedCount = 0;

    @Column(name = "status", nullable = false, length = 16)
        private String status = "AVAILABLE";

    @Column(name = "qr_code", length = 256)
    private String qrCode;

    @Column(name = "redeemed_by")
    private Long redeemedBy;

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    @Column(name = "redeem_location", length = 256)
    private String redeemLocation;

    @Column(name = "remark", length = 512)
    private String remark;

    public BenefitRedemption() {}

    // ---- Getters & Setters ----
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }
    public String getBenefitName() { return benefitName; }
    public void setBenefitName(String benefitName) { this.benefitName = benefitName; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public Long getRedeemedBy() { return redeemedBy; }
    public void setRedeemedBy(Long redeemedBy) { this.redeemedBy = redeemedBy; }
    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(LocalDateTime redeemedAt) { this.redeemedAt = redeemedAt; }
    public String getRedeemLocation() { return redeemLocation; }
    public void setRedeemLocation(String redeemLocation) { this.redeemLocation = redeemLocation; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
