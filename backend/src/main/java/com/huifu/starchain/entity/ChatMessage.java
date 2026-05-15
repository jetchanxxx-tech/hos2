package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
  public class ChatMessage extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "seq_no", nullable = false)
        private Integer seqNo = 0;

    @Column(name = "sender_type", nullable = false, length = 16)
    private String senderType;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_name", length = 64)
    private String senderName;

    @Column(name = "msg_type", nullable = false, length = 16)
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
        private Boolean triggerAlert = false;

    @Column(name = "alert_keyword", length = 128)
    private String alertKeyword;

    @Column(name = "ai_answer_source", length = 64)
    private String aiAnswerSource;

    @Column(name = "is_read")
        private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public ChatMessage() {}

    // ---- Getters & Setters ----
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaMetaJson() { return mediaMetaJson; }
    public void setMediaMetaJson(String mediaMetaJson) { this.mediaMetaJson = mediaMetaJson; }
    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
    public String getAlertKeyword() { return alertKeyword; }
    public void setAlertKeyword(String alertKeyword) { this.alertKeyword = alertKeyword; }
    public String getAiAnswerSource() { return aiAnswerSource; }
    public void setAiAnswerSource(String aiAnswerSource) { this.aiAnswerSource = aiAnswerSource; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public Integer getSeqNo() { return seqNo; }
    public void setSeqNo(Integer seqNo) { this.seqNo = seqNo; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public Boolean getTriggerAlert() { return triggerAlert; }
    public void setTriggerAlert(Boolean triggerAlert) { this.triggerAlert = triggerAlert; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
