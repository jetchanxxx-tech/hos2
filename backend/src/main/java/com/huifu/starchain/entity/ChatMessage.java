package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "seq_no", nullable = false)
    @Builder.Default
    private Integer seqNo = 0;

    @Column(name = "sender_type", nullable = false, length = 16)
    private String senderType;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_name", length = 64)
    private String senderName;

    @Column(name = "msg_type", nullable = false, length = 16)
    @Builder.Default
    private String msgType = "TEXT";

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    @Column(name = "media_meta_json", columnDefinition = "JSON")
    private String mediaMetaJson;

    @Column(name = "extra_json", columnDefinition = "JSON")
    private String extraJson;

    @Column(name = "trigger_alert")
    @Builder.Default
    private Boolean triggerAlert = false;

    @Column(name = "alert_keyword", length = 128)
    private String alertKeyword;

    @Column(name = "ai_answer_source", length = 64)
    private String aiAnswerSource;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
