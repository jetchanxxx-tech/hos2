package com.huifu.starchain.service;

import com.huifu.starchain.common.exception.BizError;
import com.huifu.starchain.common.exception.BusinessException;
import com.huifu.starchain.config.CryptoConfig.CryptoUtil;
import com.huifu.starchain.config.jwt.JwtUtil;
import com.huifu.starchain.dto.auth.*;
import com.huifu.starchain.entity.User;
import com.huifu.starchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        String phoneHash = sha256(req.phone());
        if (userRepo.findByPhoneHash(phoneHash).isPresent()) {
            throw new BusinessException(BizError.PHONE_ALREADY_EXISTS);
        }
        User user = User.builder()
                .phone(cryptoUtil.encrypt(req.phone()))
                .phoneHash(phoneHash)
                .nameMasked(cryptoUtil.maskName(req.name()))
                .realName(cryptoUtil.encrypt(req.name()))
                .openid(req.openid())
                .role(User.UserRole.RESIDENT)
                .status(User.UserStatus.ACTIVE)
                .dataAuthConsent(1)
                .build();
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
        return LoginResponse.builder()
                .accessToken(newToken)
                .refreshToken(refreshToken)
                .expiresIn(7200)
                .build();
    }

    private LoginResponse buildLoginResponse(User user, String token, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .expiresIn(7200)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getNameMasked())
                        .role(user.getRole().name())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
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
