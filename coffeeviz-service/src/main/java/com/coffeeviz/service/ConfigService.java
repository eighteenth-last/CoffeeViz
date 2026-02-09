package com.coffeeviz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.SystemConfig;
import com.coffeeviz.mapper.SystemConfigMapper;
import com.coffeeviz.util.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Autowired
    private PasswordEncryptor passwordEncryptor;
    
    // 敏感配置键（需要加密）
    private static final String[] SENSITIVE_KEYS = {
        "openai.api.key",
        "jdbc.default.password"
    };
    
    /**
     * 查询配置值
     * 
     * @param configKey 配置键
     * @return 配置值（敏感信息自动解密）
     */
    public String getConfig(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            return null;
        }
        
        String configValue = config.getConfigValue();
        
        // 如果是敏感配置，尝试解密
        if (isSensitiveKey(configKey)) {
            // 判断是否为加密数据（Base64 编码的数据不包含某些特殊字符）
            if (isEncrypted(configValue)) {
                try {
                    return passwordEncryptor.decrypt(configValue);
                } catch (Exception e) {
                    log.warn("解密配置失败，使用原始值: {}", configKey);
                    return configValue;
                }
            } else {
                // 明文存储，直接返回
                log.debug("配置为明文存储: {}", configKey);
                return configValue;
            }
        }
        
        return configValue;
    }
    
    /**
     * 查询配置值（带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfig(String configKey, String defaultValue) {
        String value = getConfig(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 查询所有配置
     * 
     * @return 配置映射（敏感信息已脱敏）
     */
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            String value = config.getConfigValue();
            
            // 敏感配置脱敏显示
            if (isSensitiveKey(config.getConfigKey())) {
                value = maskSensitiveValue(value);
            }
            
            result.put(config.getConfigKey(), value);
        }
        
        return result;
    }
    
    /**
     * 更新配置
     * 
     * @param configKey 配置键
     * @param configValue 配置值（敏感信息自动加密）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue) {
        log.info("更新配置: key={}", configKey);
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        
        // 如果是敏感配置，自动加密
        String valueToSave = configValue;
        if (isSensitiveKey(configKey)) {
            try {
                valueToSave = passwordEncryptor.encrypt(configValue);
            } catch (Exception e) {
                log.error("加密配置失败: {}", configKey, e);
                throw new RuntimeException("加密配置失败", e);
            }
        }
        
        if (config == null) {
            // 新增配置
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(valueToSave);
            systemConfigMapper.insert(config);
        } else {
            // 更新配置
            config.setConfigValue(valueToSave);
            systemConfigMapper.updateById(config);
        }
        
        log.info("配置更新成功: key={}", configKey);
    }
    
    /**
     * 批量更新配置
     * 
     * @param configs 配置映射
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 删除配置
     * 
     * @param configKey 配置键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String configKey) {
        log.info("删除配置: key={}", configKey);
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        
        systemConfigMapper.delete(wrapper);
    }
    
    /**
     * 初始化默认配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultConfigs() {
        log.info("初始化默认配置");
        
        Map<String, String> defaultConfigs = new HashMap<>();
        defaultConfigs.put("openai.api.base_url", "https://api.openai.com");
        defaultConfigs.put("openai.model.name", "gpt-4");
        defaultConfigs.put("mermaid.cli.path", "mmdc");
        defaultConfigs.put("export.max.size", "10485760"); // 10MB
        
        for (Map.Entry<String, String> entry : defaultConfigs.entrySet()) {
            String key = entry.getKey();
            
            // 检查配置是否已存在
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, key);
            
            if (systemConfigMapper.selectCount(wrapper) == 0) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(key);
                config.setConfigValue(entry.getValue());
                config.setDescription("系统默认配置");
                systemConfigMapper.insert(config);
                
                log.info("初始化配置: key={}, value={}", key, entry.getValue());
            }
        }
    }
    
    /**
     * 判断是否为敏感配置键
     */
    private boolean isSensitiveKey(String configKey) {
        for (String sensitiveKey : SENSITIVE_KEYS) {
            if (configKey.equals(sensitiveKey) || configKey.contains("password") || configKey.contains("secret")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断字符串是否为加密数据（Base64 编码）
     * Base64 只包含 A-Z, a-z, 0-9, +, /, = 字符
     * 如果包含其他字符（如 - 连字符），则不是 Base64 编码
     */
    private boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        // 检查是否只包含 Base64 字符
        // Base64 字符集: A-Z, a-z, 0-9, +, /, =
        return value.matches("^[A-Za-z0-9+/=]+$");
    }
    
    /**
     * 脱敏显示敏感值
     */
    private String maskSensitiveValue(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        
        // 显示前 2 位和后 2 位，中间用 * 代替
        int length = value.length();
        return value.substring(0, 2) + "****" + value.substring(length - 2);
    }
}
