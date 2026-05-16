package com.huifu.starchain.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易限流过滤器 — API 100 req/s, 登录 10 req/s
 */
@Component
public class RateLimitFilter implements Filter {

    private final Map<String, SlidingWindow> counters = new ConcurrentHashMap<>();

    private static class SlidingWindow {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String key = ip + ":" + path;

        int maxRequests = 100;
        if (path.contains("/login") || path.contains("/register")) maxRequests = 10;
        if (path.contains("/admin")) maxRequests = 50;

        SlidingWindow window = counters.computeIfAbsent(key, k -> new SlidingWindow());
        long now = System.currentTimeMillis();
        if (now - window.windowStart > 1000) {
            window.windowStart = now;
            window.count = 0;
        }
        window.count++;

        if (window.count > maxRequests) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":42900,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }
        chain.doFilter(req, resp);
    }
}
