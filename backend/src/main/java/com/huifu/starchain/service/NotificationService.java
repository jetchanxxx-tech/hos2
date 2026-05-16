package com.huifu.starchain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务 — 统一入口，对接 SMS/邮件/微信模板消息/App 推送
 * 当前为占位实现，记录日志；生产环境替换为真实推送通道
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<String, Long> rateLimiter = new ConcurrentHashMap<>();

    /** 发送短信 */
    public boolean sendSms(String phone, String templateId, Map<String, String> params) {
        log.info("[SMS] to={} template={} params={}", maskPhone(phone), templateId, params);
        // TODO: 接入阿里云/腾讯云短信 API
        return true;
    }

    /** 发送微信模板消息 */
    public boolean sendWechatTemplate(String openid, String templateId, Map<String, String> data) {
        log.info("[Wechat] to={} template={} data={}", openid, templateId, data);
        // TODO: 接入微信模板消息 API
        return true;
    }

    /** 发送邮件 */
    public boolean sendEmail(String to, String subject, String body) {
        log.info("[Email] to={} subject={}", to, subject);
        // TODO: 接入 SMTP / SendGrid / 阿里云邮件推送
        return true;
    }

    /** 应用内通知（通过 ChatService 创建系统消息） */
    public boolean sendInApp(Long userId, String title, String content) {
        log.info("[InApp] userId={} title={} content={}", userId, title);
        // 通过 ChatService.startSession + sendMessage 实现站内信
        return true;
    }

    /** 紧急通知（绕过限频，所有通道同时推送） */
    public boolean sendUrgent(Long userId, String title, String content) {
        String key = "urgent:" + userId;
        long now = System.currentTimeMillis();
        Long last = rateLimiter.get(key);
        if (last != null && now - last < 600_000) { // 10 分钟内不重复
            log.warn("Urgent notification rate-limited for user {}", userId);
            return false;
        }
        rateLimiter.put(key, now);
        log.warn("[URGENT] userId={} title={} content={}", userId, title, content);
        sendSms("placeholder", "URGENT_ALERT", Map.of("title", title, "content", content));
        sendInApp(userId, title, content);
        return true;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
