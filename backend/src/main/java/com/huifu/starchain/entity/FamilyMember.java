package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_members", uniqueConstraints = @UniqueConstraint(columnNames = {"familyId", "userId"}))
  public class FamilyMember extends BaseEntity {

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public FamilyMember() {}

    // ---- Getters & Setters ----
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Relationship getRelationship() { return relationship; }
    public void setRelationship(Relationship relationship) { this.relationship = relationship; }
    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }
    public Boolean getIsEmergencyContact() { return isEmergencyContact; }
    public void setIsEmergencyContact(Boolean isEmergencyContact) { this.isEmergencyContact = isEmergencyContact; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    @Column(name = "relationship", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
        private Relationship relationship = Relationship.SELF;

    @Column(name = "share_scope", nullable = false, length = 32)
        private String shareScope = "ALL";

    @Column(name = "is_emergency_contact")
        private Boolean isEmergencyContact = false;

    @Column(name = "joined_at", nullable = false)
        private LocalDateTime joinedAt = LocalDateTime.now();

    public enum Relationship { SELF, SPOUSE, CHILD, PARENT, OTHER }
}
