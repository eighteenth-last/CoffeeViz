package com.coffeeviz.dto;

import lombok.Data;

import java.util.Map;

/**
 * 配置更新请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class ConfigUpdateRequest {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 批量配置（用于批量更新）
     */
    private Map<String, String> configs;
}
