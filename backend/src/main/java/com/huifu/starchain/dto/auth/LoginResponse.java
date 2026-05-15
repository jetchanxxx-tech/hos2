package com.huifu.starchain.dto.auth;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserInfo user
) {
    public record UserInfo(Long id, String name, String role, String avatarUrl) {}
}
