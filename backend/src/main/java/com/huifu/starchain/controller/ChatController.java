package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.common.response.PageResult;
import com.huifu.starchain.entity.*;
import com.huifu.starchain.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public ApiResponse<ChatSession> startSession(@AuthenticationPrincipal Long userId,
                                                  @RequestParam(defaultValue = "MINIPROGRAM") String channel) {
        return ApiResponse.ok(chatService.startSession(userId, channel));
    }

    @GetMapping("/sessions")
    public ApiResponse<PageResult<ChatSession>> mySessions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(chatService.getMySessions(userId, page, size));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        return ApiResponse.ok(chatService.getMessages(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<ChatMessage> sendMessage(@PathVariable Long sessionId,
                                                 @RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal Long userId) {
        ChatMessage msg = chatService.sendMessage(sessionId,
                body.getOrDefault("senderType", "USER"),
                userId,
                body.getOrDefault("senderName", ""),
                body.getOrDefault("msgType", "TEXT"),
                body.get("content"));
        // Auto AI reply for non-emergency queries
        if (msg != null && !Boolean.TRUE.equals(msg.getTriggerAlert())) {
            String answer = chatService.findBestAnswer(body.getOrDefault("content", ""));
            if (answer != null) {
                chatService.sendAiReply(sessionId, answer, "FAQ");
            }
        }
        return ApiResponse.ok(msg);
    }

    @PutMapping("/sessions/{sessionId}/escalate")
    public ApiResponse<ChatSession> escalate(@PathVariable Long sessionId,
                                              @RequestParam(defaultValue = "BUTLER") String level) {
        return ApiResponse.ok(chatService.escalateSession(sessionId, level));
    }

    @PutMapping("/sessions/{sessionId}/resolve")
    public ApiResponse<ChatSession> resolve(@PathVariable Long sessionId,
                                             @AuthenticationPrincipal Long userId,
                                             @RequestParam(required = false) Integer satisfaction) {
        return ApiResponse.ok(chatService.resolveSession(sessionId, userId, satisfaction));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<ChatMessage>> getAlerts() {
        return ApiResponse.ok(chatService.getAlerts());
    }

    // Knowledge Base
    @GetMapping("/knowledge")
    public ApiResponse<PageResult<KnowledgeArticle>> searchKnowledge(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(chatService.searchKnowledge(keyword, page, size));
    }

    @GetMapping("/knowledge/category/{category}")
    public ApiResponse<List<KnowledgeArticle>> getByCategory(@PathVariable String category) {
        return ApiResponse.ok(chatService.searchByCategory(category));
    }
}
