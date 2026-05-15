package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "user_role", length = 24)
    private String userRole;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @Column(name = "resource_desc", length = 256)
    private String resourceDesc;

    @Column(name = "old_value_json", columnDefinition = "JSON")
    private String oldValueJson;

    @Column(name = "new_value_json", columnDefinition = "JSON")
    private String newValueJson;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "request_url", length = 512)
    private String requestUrl;

    @Column(name = "request_method", length = 8)
    private String requestMethod;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "result", nullable = false, length = 16)
    @Builder.Default
    private String result = "SUCCESS";

    @Column(name = "error_msg", length = 1024)
    private String errorMsg;
}
