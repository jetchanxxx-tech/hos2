package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @NoArgsConstructor public class User extends BaseEntity {

    @Column(name = "openid", unique = true)
    private String openid;

    @Column(name = "unionid")
    private String unionid;

    @Column(name = "wecom_id", unique = true)
    private String wecomId;

    @Column(name = "phone", nullable = false, length = 256)
    private String phone;

    @Column(name = "phone_hash", nullable = false, unique = true, length = 64)
    private String phoneHash;

    @Column(name = "name_masked", nullable = false, length = 64)
    private String nameMasked;

    @Column(name = "real_name", length = 256)
    private String realName;

    @Column(name = "id_card_enc", length = 512)
    private String idCardEnc;

    @Column(name = "id_card_hash", length = 64)
    private String idCardHash;

    @Column(name = "gender")
        private Integer gender = 0;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "role", nullable = false, length = 24)
    @Enumerated(EnumType.STRING)
        private UserRole role = UserRole.RESIDENT;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
        private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "hospital_patient_id")
    private String hospitalPatientId;

    @Column(name = "data_auth_consent")
        private Integer dataAuthConsent = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    public User() {}

    // ---- Getters & Setters ----
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    public String getWecomId() { return wecomId; }
    public void setWecomId(String wecomId) { this.wecomId = wecomId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhoneHash() { return phoneHash; }
    public void setPhoneHash(String phoneHash) { this.phoneHash = phoneHash; }
    public String getNameMasked() { return nameMasked; }
    public void setNameMasked(String nameMasked) { this.nameMasked = nameMasked; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCardEnc() { return idCardEnc; }
    public void setIdCardEnc(String idCardEnc) { this.idCardEnc = idCardEnc; }
    public String getIdCardHash() { return idCardHash; }
    public void setIdCardHash(String idCardHash) { this.idCardHash = idCardHash; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public String getHospitalPatientId() { return hospitalPatientId; }
    public void setHospitalPatientId(String hospitalPatientId) { this.hospitalPatientId = hospitalPatientId; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }

    public enum UserRole {
        RESIDENT, BUTLER_MEDICAL, BUTLER_SERVICE, HOSPITAL_ADMIN, OPS_ADMIN, SUPER_ADMIN
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
