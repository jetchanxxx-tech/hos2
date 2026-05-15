package com.huifu.starchain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_articles")
@Getter @NoArgsConstructor public class KnowledgeArticle extends BaseEntity {

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;

    @Column(name = "question", nullable = false, length = 512)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answer_type", nullable = false, length = 16)
        private String answerType = "TEXT";

    @Column(name = "media_urls_json", columnDefinition = "JSON")
    private String mediaUrlsJson;

    @Column(name = "view_count")
        private Integer viewCount = 0;

    @Column(name = "helpful_count")
        private Integer helpfulCount = 0;

    @Column(name = "status", nullable = false, length = 16)
        private String status = "PUBLISHED";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public KnowledgeArticle() {}

    // ---- Getters & Setters ----
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTagsJson() { return tagsJson; }
    public void setTagsJson(String tagsJson) { this.tagsJson = tagsJson; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getMediaUrlsJson() { return mediaUrlsJson; }
    public void setMediaUrlsJson(String mediaUrlsJson) { this.mediaUrlsJson = mediaUrlsJson; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
