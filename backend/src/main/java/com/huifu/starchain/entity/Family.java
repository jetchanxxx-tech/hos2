package com.huifu.starchain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "families")
@Getter @NoArgsConstructor public class Family extends BaseEntity {

    @Column(name = "family_name", nullable = false, length = 128)
    private String familyName;

    @Column(name = "primary_user_id", nullable = false)
    private Long primaryUserId;

    @Column(name = "member_count")
        private Integer memberCount = 1;

    @Column(name = "invite_code", unique = true, length = 16)
    private String inviteCode;

    public Family() {}

    // ---- Getters & Setters ----
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public Long getPrimaryUserId() { return primaryUserId; }
    public void setPrimaryUserId(Long primaryUserId) { this.primaryUserId = primaryUserId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    @Column(name = "share_policy", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
        private SharePolicy sharePolicy = SharePolicy.PRIMARY_ONLY;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
        private FamilyStatus status = FamilyStatus.ACTIVE;

    public enum SharePolicy { PRIMARY_ONLY, FULL_SHARE, CUSTOM }
    public enum FamilyStatus { ACTIVE, DISSOLVED }
}
