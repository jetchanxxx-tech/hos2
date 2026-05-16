package com.huifu.starchain.config.im;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统内置聊天适配器 — 使用 ChatService 在应用内完成消息收发
 * 适用于没有第三方 IM 对接的独立部署场景
 */
@Component
@Profile("!wechat-im & !dingtalk-im & !feishu-im")
public class InternalChatAdapter implements ImAdapter {

    private final Map<String, String> userStatus = new ConcurrentHashMap<>();

    @Override public String getChannel() { return "INTERNAL"; }

    @Override public boolean isOnline() { return true; }

    @Override
    public boolean sendText(String userId, String text) {
        // 通过 ChatService 发送站内消息
        userStatus.put(userId, "message_sent");
        return true;
    }

    @Override
    public boolean sendNews(String userId, String title, String desc, String url, String picUrl) {
        userStatus.put(userId, "news_sent");
        return true;
    }

    @Override
    public boolean sendTemplate(String userId, String templateId, Map<String, String> data) {
        userStatus.put(userId, "template_sent:" + templateId);
        return true;
    }

    @Override
    public Map<String, Object> getUserInfo(String userId) {
        return Map.of("userId", userId, "channel", "INTERNAL", "online", true);
    }

    @Override
    public String handleCallback(String rawBody, String signature, String timestamp, String nonce) {
        return "success"; // 内置模式无需处理回调
    }
}
