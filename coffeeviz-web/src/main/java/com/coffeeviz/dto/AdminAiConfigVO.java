package com.coffeeviz.dto;

import lombok.Data;

/**
 * 管理端AI配置
 */
@Data
public class AdminAiConfigVO {
    private String apiEndpoint;
    private String apiKey;
    private String modelName;
    private Integer maxTokens;
}
