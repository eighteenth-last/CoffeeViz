package com.coffeeviz.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 生成响应
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    
    /**
     * 生成的 SQL DDL
     */
    private String sqlDdl;
    
    /**
     * 业务说明
     */
    private String explanation;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 建议列表
     */
    private List<String> suggestions;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误消息
     */
    private String errorMessage;
}
