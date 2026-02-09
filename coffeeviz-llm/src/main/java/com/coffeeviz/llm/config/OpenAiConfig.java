package com.coffeeviz.llm.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI 配置（从数据库 sys_config 表读取）
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Configuration
public class OpenAiConfig {
    
    /**
     * API Key（从数据库读取：openai.api.key）
     */
    private String key;
    
    /**
     * API Base URL（从数据库读取：openai.api.base_url）
     */
    private String baseUrl = "https://api.openai.com";
    
    /**
     * 模型名称（从数据库读取：openai.model.name）
     */
    private String model = "gpt-4";
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 30000;
    
    /**
     * 最大 Token 数
     */
    private Integer maxTokens = 2000;
    
    /**
     * 温度参数（0-2）
     */
    private Double temperature = 0.7;
}
