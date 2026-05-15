package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatSession extends BaseEntity {

    @Column(name = "session_no", nullable = false, unique = true, length = 32)
    private String sessionNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "channel", nullable = false, length = 16)
    @Builder.Default
    private String channel = "MINIPROGRAM";

    @Column(name = "intent_type", length = 32)
    private String intentType;

    @Column(name = "intent_confidence", precision = 5, scale = 4)
    private BigDecimal intentConfidence;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "escalation_level", nullable = false, length = 16)
    @Builder.Default
    private String escalationLevel = "NONE";

    @Column(name = "sentiment_score", precision = 5, scale = 4)
    private BigDecimal sentimentScore;

    @Column(name = "sentiment_label", length = 16)
    private String sentimentLabel;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;
}
