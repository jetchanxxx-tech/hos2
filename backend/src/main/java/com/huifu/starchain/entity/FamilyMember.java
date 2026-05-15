package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_members", uniqueConstraints = @UniqueConstraint(columnNames = {"familyId", "userId"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FamilyMember extends BaseEntity {

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "relationship", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Relationship relationship = Relationship.SELF;

    @Column(name = "share_scope", nullable = false, length = 32)
    @Builder.Default
    private String shareScope = "ALL";

    @Column(name = "is_emergency_contact")
    @Builder.Default
    private Boolean isEmergencyContact = false;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum Relationship { SELF, SPOUSE, CHILD, PARENT, OTHER }
}
