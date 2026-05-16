package com.huifu.starchain.config.im;

import java.util.Map;

/**
 * IM 适配器接口 — 预留对接微信/QQ/飞书等多渠道
 * 实现类通过 @Conditional 按配置激活
 */
public interface ImAdapter {

    /** 渠道标识 */
    String getChannel();

    /** 是否已连接/在线 */
    boolean isOnline();

    /** 发送文本消息到指定用户 */
    boolean sendText(String userId, String text);

    /** 发送图文消息 */
    boolean sendNews(String userId, String title, String desc, String url, String picUrl);

    /** 推送模板消息 */
    boolean sendTemplate(String userId, String templateId, Map<String, String> data);

    /** 拉取用户信息 */
    Map<String, Object> getUserInfo(String userId);

    /** 接收回调消息(由 Controller 转发到此) */
    String handleCallback(String rawBody, String signature, String timestamp, String nonce);
}
