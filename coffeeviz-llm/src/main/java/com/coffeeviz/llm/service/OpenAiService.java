package com.coffeeviz.llm.service;

import com.coffeeviz.llm.model.AiRequest;
import com.coffeeviz.llm.model.AiResponse;

/**
 * OpenAI 服务接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public interface OpenAiService {
    
    /**
     * 根据自然语言描述生成 SQL DDL
     * 
     * @param request AI 请求
     * @return AI 响应
     */
    AiResponse generateSqlFromPrompt(AiRequest request);
    
    /**
     * 检查 API 配置是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
}
