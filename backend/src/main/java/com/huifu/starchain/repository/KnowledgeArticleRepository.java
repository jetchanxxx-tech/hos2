package com.huifu.starchain.repository;

import com.huifu.starchain.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    Page<KnowledgeArticle> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<KnowledgeArticle> findByCategoryAndStatus(String category, String status, Pageable pageable);

    @Query(value = "SELECT * FROM knowledge_articles WHERE status = 'PUBLISHED' AND MATCH(question, answer) AGAINST(?1 IN NATURAL LANGUAGE MODE) LIMIT ?2", nativeQuery = true)
    List<KnowledgeArticle> searchByFulltext(String keyword, int limit);

    @Query("SELECT ka FROM KnowledgeArticle ka WHERE ka.status = 'PUBLISHED' AND (ka.question LIKE %:keyword% OR ka.title LIKE %:keyword%)")
    List<KnowledgeArticle> searchByKeyword(String keyword, Pageable pageable);
}
