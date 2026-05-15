package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "service_packages")
@Getter @NoArgsConstructor public class ServicePackage extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "subtitle", length = 256)
    private String subtitle;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "category", nullable = false, length = 32)
        private String category = "MATERNITY";

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "duration_days")
        private Integer durationDays = 365;

    @Column(name = "max_beneficiaries")
        private Integer maxBeneficiaries = 1;

    @Column(name = "cover_image_url", length = 512)
    private String coverImageUrl;

    @Column(name = "benefits_json", nullable = false, columnDefinition = "JSON")
    private String benefitsJson;

    @Column(name = "terms_text", columnDefinition = "TEXT")
    private String termsText;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
        private PkgStatus status = PkgStatus.DRAFT;

    @Column(name = "sort_order")
        private Integer sortOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;

    public ServicePackage() {}

    // ---- Getters & Setters ----
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getBenefitsJson() { return benefitsJson; }
    public void setBenefitsJson(String benefitsJson) { this.benefitsJson = benefitsJson; }
    public String getTermsText() { return termsText; }
    public void setTermsText(String termsText) { this.termsText = termsText; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public enum PkgStatus { DRAFT, ON_SHELF, OFF_SHELF }
}
