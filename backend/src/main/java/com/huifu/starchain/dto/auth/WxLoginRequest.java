package com.huifu.starchain.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record WxLoginRequest(
    @NotBlank String code,
    String encryptedData,
    String iv
) {}
