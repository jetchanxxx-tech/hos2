package com.huifu.starchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Value("${crypto.aes-key}")
    private String aesKey;

    @Bean
    public CryptoUtil cryptoUtil() {
        return new CryptoUtil(aesKey);
    }

    public static class CryptoUtil {
        private final byte[] keyBytes;

        public CryptoUtil(String rawKey) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                this.keyBytes = sha.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("Failed to init CryptoUtil", e);
            }
        }

        public String encrypt(String plainText) {
            try {
                SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                throw new RuntimeException("Encryption failed", e);
            }
        }

        public String decrypt(String encrypted) {
            try {
                SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Decryption failed", e);
            }
        }

        public String maskName(String name) {
            if (name == null || name.length() <= 1) return name;
            if (name.length() == 2) return name.charAt(0) + "*";
            return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
        }

        public String maskPhone(String phone) {
            if (phone == null || phone.length() < 7) return phone;
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
    }
}
