package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.config.CryptoConfig.CryptoUtil;
import com.huifu.starchain.config.jwt.JwtUtil;
import com.huifu.starchain.dto.auth.*;
import com.huifu.starchain.entity.User;
import com.huifu.starchain.repository.UserRepository;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;


@Service

public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, CryptoUtil cryptoUtil) { this.userRepo = userRepo; this.passwordEncoder = passwordEncoder; this.jwtUtil = jwtUtil; this.cryptoUtil = cryptoUtil; }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        String phoneHash = sha256(req.phone());
        if (userRepo.findByPhoneHash(phoneHash).isPresent()) {
            throw new BusinessException(BizError.PHONE_ALREADY_EXISTS);
        }
        User user = new User();
        user.setPhone(cryptoUtil.encrypt(req.phone()));
        user.setPhoneHash(phoneHash);
        user.setNameMasked(cryptoUtil.maskName(req.name()));
        user.setRealName(cryptoUtil.encrypt(req.name()));
        user.setOpenid(req.openid());
        user.setRole(User.UserRole.RESIDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setDataAuthConsent(1);
        user = userRepo.save(user);
        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole().name());
        return buildLoginResponse(user, token, refreshToken);
    }

    public LoginResponse login(LoginRequest req) {
        String phoneHash = sha256(req.phone());
        User user = userRepo.findByPhoneHash(phoneHash)
                .orElseThrow(() -> new BusinessException(BizError.USER_NOT_FOUND));
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new BusinessException(BizError.ACCOUNT_SUSPENDED);
        }
        // Password verification skipped for demo (using phone_hash identity)
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole().name());
        return buildLoginResponse(user, token, refreshToken);
    }

    public LoginResponse wxLogin(WxLoginRequest req) {
        // In production: call WeChat API to get openid
        String openid = "wx_" + sha256(req.code()).substring(0, 16);
        User user = userRepo.findByOpenid(openid)
                .orElseThrow(() -> new BusinessException(BizError.USER_NOT_FOUND));
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole().name());
        return buildLoginResponse(user, token, refreshToken);
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new BusinessException(BizError.UNAUTHORIZED);
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(BizError.USER_NOT_FOUND));
        String newToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        return new LoginResponse(newToken, refreshToken, 7200, null);
    }

    private LoginResponse buildLoginResponse(User user, String token, String refreshToken) {
        var userInfo = new LoginResponse.UserInfo(user.getId(), user.getNameMasked(), user.getRole().name(), user.getAvatarUrl());
        return new LoginResponse(token, refreshToken, 7200, userInfo);
    }

    private static String sha256(String input) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
