package com.huifu.starchain.repository;

import com.huifu.starchain.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionNo(String sessionNo);
    Page<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    Page<ChatSession> findByStatusAndEscalationLevelNotOrderByUpdatedAtAsc(String status, String escalationLevel, Pageable pageable);
    List<ChatSession> findByStatusAndIntentType(String status, String intentType);

    Page<ChatSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    @Query("SELECT COALESCE(AVG(cs.satisfactionScore), 0) FROM ChatSession cs WHERE cs.satisfactionScore IS NOT NULL")
    Double avgSatisfaction();

    @Query("SELECT cs.satisfactionScore, COUNT(cs) FROM ChatSession cs WHERE cs.satisfactionScore IS NOT NULL GROUP BY cs.satisfactionScore")
    List<Object[]> satisfactionDistribution();
}
