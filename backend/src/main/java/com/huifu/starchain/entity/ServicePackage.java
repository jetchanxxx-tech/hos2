package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "service_packages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ServicePackage extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "subtitle", length = 256)
    private String subtitle;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "category", nullable = false, length = 32)
    @Builder.Default
    private String category = "MATERNITY";

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "duration_days")
    @Builder.Default
    private Integer durationDays = 365;

    @Column(name = "max_beneficiaries")
    @Builder.Default
    private Integer maxBeneficiaries = 1;

    @Column(name = "cover_image_url", length = 512)
    private String coverImageUrl;

    @Column(name = "benefits_json", nullable = false, columnDefinition = "JSON")
    private String benefitsJson;

    @Column(name = "terms_text", columnDefinition = "TEXT")
    private String termsText;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PkgStatus status = PkgStatus.DRAFT;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;

    public enum PkgStatus { DRAFT, ON_SHELF, OFF_SHELF }
}
