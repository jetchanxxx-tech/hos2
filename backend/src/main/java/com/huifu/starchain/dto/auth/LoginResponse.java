package com.huifu.starchain.dto.auth;

import lombok.Builder;

@Builder
public record LoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserInfo user
) {
    @Builder
    public record UserInfo(Long id, String name, String role, String avatarUrl) {}
}
