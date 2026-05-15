package com.huifu.starchain.repository;

import com.huifu.starchain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderBySeqNoAsc(Long sessionId);
    List<ChatMessage> findByTriggerAlertTrueOrderByCreatedAtDesc();
}
