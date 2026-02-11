package com.coffeeviz.llm.service;

import com.coffeeviz.llm.model.AiRequest;
import com.coffeeviz.llm.model.AiResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * 流式生成 SQL DDL
     *
     * @param request AI 请求
     * @param emitter SSE 发射器
     */
    void generateSqlFromPromptStream(AiRequest request, SseEmitter emitter);

    /**
     * 流式生成 SQL DDL（带完成回调）
     *
     * @param request AI 请求
     * @param emitter SSE 发射器
     * @param onComplete 流式完成后的回调，参数为完整的 LLM 输出文本
     */
    void generateSqlFromPromptStream(AiRequest request, SseEmitter emitter, java.util.function.Consumer<String> onComplete);

    /**
     * 检查 API 配置是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}

