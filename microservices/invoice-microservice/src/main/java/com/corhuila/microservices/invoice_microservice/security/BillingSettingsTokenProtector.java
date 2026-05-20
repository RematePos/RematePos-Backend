package com.corhuila.microservices.invoice_microservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class BillingSettingsTokenProtector {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${billing.settings.encryption-key:}")
    private String encryptionKey;

    public String encrypt(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("Token is required");
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array());
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException("Billing settings encryption is not available");
        }
    }

    public String decrypt(String encryptedToken) {
        if (isBlank(encryptedToken)) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedToken);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalArgumentException("Billing settings token cannot be decrypted");
        }
    }

    public boolean canEncrypt() {
        try {
            keySpec();
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String mask(String token) {
        if (isBlank(token)) {
            return null;
        }
        String trimmed = token.trim();
        String suffix = trimmed.length() <= 4 ? trimmed : trimmed.substring(trimmed.length() - 4);
        return "************" + suffix;
    }

    private SecretKeySpec keySpec() {
        byte[] key = keyBytes();
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("Billing settings encryption key must be 16, 24, or 32 bytes");
        }
        return new SecretKeySpec(key, AES);
    }

    private byte[] keyBytes() {
        if (isBlank(encryptionKey)) {
            throw new IllegalArgumentException("Billing settings encryption key is not configured");
        }
        String trimmed = encryptionKey.trim();
        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // Plain placeholder keys are accepted for local/demo use when they have a valid AES length.
        }
        return trimmed.getBytes(StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
