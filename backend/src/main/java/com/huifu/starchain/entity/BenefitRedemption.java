package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "benefit_redemptions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BenefitRedemption extends BaseEntity {

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
    @Builder.Default
    private Integer totalCount = 1;

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
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
}
