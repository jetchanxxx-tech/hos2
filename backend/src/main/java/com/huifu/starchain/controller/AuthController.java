package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import com.huifu.starchain.dto.auth.*;
import com.huifu.starchain.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/wx-login")
    public ApiResponse<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest req) {
        return ApiResponse.ok(authService.wxLogin(req));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody String refreshToken) {
        return ApiResponse.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("Huifu StarChain API is running");
    }
}
