package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "package_orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
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
    @Builder.Default
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
}
