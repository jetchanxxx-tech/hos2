package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity {

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
    @Builder.Default
    private Integer gender = 0;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "role", nullable = false, length = 24)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.RESIDENT;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "hospital_patient_id")
    private String hospitalPatientId;

    @Column(name = "data_auth_consent")
    @Builder.Default
    private Integer dataAuthConsent = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    public enum UserRole {
        RESIDENT, BUTLER_MEDICAL, BUTLER_SERVICE, HOSPITAL_ADMIN, OPS_ADMIN, SUPER_ADMIN
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
