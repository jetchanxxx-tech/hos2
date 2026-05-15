package com.huifu.starchain.service;

import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.ChatMessage;
import com.huifu.starchain.entity.ChatSession;
import com.huifu.starchain.entity.KnowledgeArticle;
import com.huifu.starchain.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository msgRepo;
    private final KnowledgeArticleRepository knowledgeRepo;

    public ChatService(ChatSessionRepository sessionRepo, ChatMessageRepository msgRepo,
                       KnowledgeArticleRepository knowledgeRepo) {
        this.sessionRepo = sessionRepo; this.msgRepo = msgRepo; this.knowledgeRepo = knowledgeRepo;
    }

    @Transactional
    public ChatSession startSession(Long userId, String channel) {
        String sessionNo = "HS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", System.currentTimeMillis() % 10000);
        ChatSession session = new ChatSession();
        session.setSessionNo(sessionNo);
        session.setUserId(userId);
        session.setChannel(channel);
        session.setStatus("ACTIVE");
        session.setEscalationLevel("NONE");
        return sessionRepo.save(session);
    }

    @Transactional
    public ChatMessage sendMessage(Long sessionId, String senderType, Long senderId,
                                    String senderName, String msgType, String content) {
        ChatSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session == null) return null;
        long count = msgRepo.findBySessionIdOrderBySeqNoAsc(sessionId).size();
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSeqNo((int) count + 1);
        msg.setSenderType(senderType);
        msg.setSenderId(senderId);
        msg.setSenderName(senderName);
        msg.setMsgType(msgType);
        msg.setContent(content);
        if (content != null && containsEmergencyKeyword(content)) {
            msg.setTriggerAlert(true);
            msg.setAlertKeyword(extractKeyword(content));
            session.setEscalationLevel("URGENT");
            session.setStatus("WAITING_BUTLER");
            sessionRepo.save(session);
        }
        // 意图识别与情绪检测
        if (content != null) {
            session.setIntentType(detectIntent(content));
            session.setSentimentLabel(detectSentiment(content));
        }
        return msgRepo.save(msg);
    }

    @Transactional
    public ChatMessage sendAiReply(Long sessionId, String content, String source) {
        ChatSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session == null) return null;
        long count = msgRepo.findBySessionIdOrderBySeqNoAsc(sessionId).size();
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSeqNo((int) count + 1);
        msg.setSenderType("AI");
        msg.setSenderName("惠福灵犀");
        msg.setMsgType("TEXT");
        msg.setContent(content);
        msg.setAiAnswerSource(source);
        return msgRepo.save(msg);
    }

    public PageResult<ChatSession> getMySessions(Long userId, int page, int size) {
        var pg = sessionRepo.findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public List<ChatMessage> getMessages(Long sessionId) {
        return msgRepo.findBySessionIdOrderBySeqNoAsc(sessionId);
    }

    public List<ChatMessage> getAlerts() {
        return msgRepo.findByTriggerAlertTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public ChatSession escalateSession(Long sessionId, String level) {
        ChatSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setEscalationLevel(level);
            session.setStatus("WAITING_BUTLER");
            sessionRepo.save(session);
        }
        return session;
    }

    @Transactional
    public ChatSession resolveSession(Long sessionId, Long resolvedBy, Integer satisfaction) {
        ChatSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setStatus("RESOLVED");
            session.setResolvedBy(resolvedBy);
            session.setResolvedAt(LocalDateTime.now());
            session.setSatisfactionScore(satisfaction);
            sessionRepo.save(session);
        }
        return session;
    }

    public PageResult<KnowledgeArticle> searchKnowledge(String keyword, int page, int size) {
        var pg = knowledgeRepo.searchByKeyword(keyword, PageRequest.of(page - 1, size));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), page, size);
    }

    public List<KnowledgeArticle> searchByCategory(String category) {
        return knowledgeRepo.findByCategoryAndStatus(category, "PUBLISHED", PageRequest.of(0, 50)).getContent();
    }

    public String findBestAnswer(String question) {
        List<KnowledgeArticle> matches = knowledgeRepo.searchByFulltext(question, 3);
        if (!matches.isEmpty()) return matches.get(0).getAnswer();
        return null;
    }

    private boolean containsEmergencyKeyword(String text) {
        for (String kw : new String[]{"出血","剧痛","破水","晕倒","抽搐","呼吸困难","大出血"})
            if (text.contains(kw)) return true;
        return false;
    }

    private String extractKeyword(String text) {
        for (String kw : new String[]{"出血","剧痛","破水","晕倒","抽搐","呼吸困难","大出血"})
            if (text.contains(kw)) return kw;
        return null;
    }

    /** 意图识别（基于关键词规则，P2 升级为 LLM） */
    public String detectIntent(String text) {
        String t = text.toLowerCase();
        if (containsKeyword(t, "痛","疼","出血","发烧","咳嗽","吐","晕","药","检查","复查","手术","指标","病","诊")) return "MEDICAL";
        if (containsKeyword(t, "券","核销","预约","陪诊","套餐","退","服务包","钱","退费","权益")) return "BENEFIT";
        if (containsKeyword(t, "投诉","差","火大","等太久","态度","敷衍","退款","举报")) return "COMPLAINT";
        if (containsKeyword(t, "挂号","下次","时间","约","改期","什么时候")) return "APPOINTMENT";
        return "GENERAL";
    }

    /** 情绪检测（基于关键词规则，P2 升级为 LLM） */
    public String detectSentiment(String text) {
        if (containsKeyword(text, "投诉","火大","差劲","烂","退款","举报","敷衍","气死","操","cnm")) return "ANGRY";
        if (containsKeyword(text, "担心","怕","焦虑","紧张","严重","危险","怎么办","会不会")) return "ANXIOUS";
        if (containsKeyword(text, "谢谢","感谢","好","棒","满意","开心","太好了")) return "POSITIVE";
        return "NEUTRAL";
    }

    private boolean containsKeyword(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }
}
