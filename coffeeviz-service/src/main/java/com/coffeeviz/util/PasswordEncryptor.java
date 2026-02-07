package com.coffeeviz.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 密码加密工具类
 * 使用 AES 算法加密/解密敏感信息（如 JDBC 密码）
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class PasswordEncryptor {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    @Value("${app.security.aes-key:coffeeviz-secret-key-32-chars}")
    private String aesKey;
    
    /**
     * AES 加密
     * 
     * @param plainText 明文
     * @return 加密后的 Base64 字符串
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // 确保密钥长度为 16 字节（128 位）
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * AES 解密
     * 
     * @param encryptedText 加密后的 Base64 字符串
     * @return 明文
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // 确保密钥长度为 16 字节（128 位）
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取密钥字节数组（确保 16 字节）
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
        
        // 如果密钥长度不足 16 字节，补齐
        if (keyBytes.length < 16) {
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return paddedKey;
        }
        
        // 如果密钥长度超过 16 字节，截取前 16 字节
        if (keyBytes.length > 16) {
            byte[] truncatedKey = new byte[16];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 16);
            return truncatedKey;
        }
        
        return keyBytes;
    }
}
