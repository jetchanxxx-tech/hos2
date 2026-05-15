package com.huifu.starchain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_articles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KnowledgeArticle extends BaseEntity {

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
    @Builder.Default
    private String answerType = "TEXT";

    @Column(name = "media_urls_json", columnDefinition = "JSON")
    private String mediaUrlsJson;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
