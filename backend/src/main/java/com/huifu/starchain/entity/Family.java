package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "families")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Family extends BaseEntity {

    @Column(name = "family_name", nullable = false, length = 128)
    private String familyName;

    @Column(name = "primary_user_id", nullable = false)
    private Long primaryUserId;

    @Column(name = "member_count")
    @Builder.Default
    private Integer memberCount = 1;

    @Column(name = "invite_code", unique = true, length = 16)
    private String inviteCode;

    @Column(name = "share_policy", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SharePolicy sharePolicy = SharePolicy.PRIMARY_ONLY;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FamilyStatus status = FamilyStatus.ACTIVE;

    public enum SharePolicy { PRIMARY_ONLY, FULL_SHARE, CUSTOM }
    public enum FamilyStatus { ACTIVE, DISSOLVED }
}
