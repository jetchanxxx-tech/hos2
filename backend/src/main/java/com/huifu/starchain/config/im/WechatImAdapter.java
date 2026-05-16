package com.huifu.starchain.config.im;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信/企业微信 IM 适配器 — 预留实现
 * 激活方式: spring.profiles.active=prod,wechat-im
 *
 * 对接微信客服消息 API / 企业微信会话存档 API
 */
@Component
@Profile("wechat-im")
public class WechatImAdapter implements ImAdapter {

    @Value("${wechat.miniprogram.app-id:}") private String appId;
    @Value("${wechat.miniprogram.app-secret:}") private String appSecret;
    @Value("${wecom.corp-id:}") private String corpId;

    @Override public String getChannel() { return "WECHAT"; }

    @Override
    public boolean isOnline() {
        return !appId.isEmpty() && !appSecret.isEmpty();
    }

    @Override
    public boolean sendText(String userId, String text) {
        // TODO: 调用微信客服消息 API POST https://api.weixin.qq.com/cgi-bin/message/custom/send
        // access_token = getAccessToken()
        // body: {"touser": userId, "msgtype": "text", "text": {"content": text}}
        return true;
    }

    @Override
    public boolean sendNews(String userId, String title, String desc, String url, String picUrl) {
        // TODO: 发送图文消息
        return true;
    }

    @Override
    public boolean sendTemplate(String userId, String templateId, Map<String, String> data) {
        // TODO: 调用微信模板消息 API
        return true;
    }

    @Override
    public Map<String, Object> getUserInfo(String userId) {
        // TODO: 调用微信用户信息 API
        return Map.of("userId", userId, "channel", "WECHAT");
    }

    @Override
    public String handleCallback(String rawBody, String signature, String timestamp, String nonce) {
        // TODO: 验证签名 + 解密消息 + 转发到 ChatService
        return "success";
    }
}
