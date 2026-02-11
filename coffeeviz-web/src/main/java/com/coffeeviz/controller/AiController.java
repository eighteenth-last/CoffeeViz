package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.annotation.RateLimit;
import com.coffeeviz.annotation.RequireQuota;
import com.coffeeviz.annotation.RequireSubscription;
import com.coffeeviz.common.Result;
import com.coffeeviz.core.model.RenderOptions;
import com.coffeeviz.dto.ErResponse;
import com.coffeeviz.llm.model.AiRequest;
import com.coffeeviz.llm.model.AiResponse;
import com.coffeeviz.llm.service.OpenAiService;
import com.coffeeviz.service.ErService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 生成 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private OpenAiService openAiService;
    
    @Autowired
    private ErService erService;
    
    @Autowired
    private com.coffeeviz.service.ConfigService configService;
    
    @Autowired
    private com.coffeeviz.service.QuotaService quotaService;
    
    @Autowired
    private com.coffeeviz.service.SubscriptionService subscriptionService;
    
    /**
     * AI 生成 SQL 并渲染 ER 图
     * 需要 AI 功能权限且消耗 ai_generate 配额
     */
    @PostMapping("/generate")
    @RequireSubscription(feature = "ai")
    @RequireQuota("ai_generate")
    @RateLimit(key = "ai_generate", time = 60, count = 10, limitType = RateLimit.LimitType.USER)
    public Result<ErResponse> generateFromAi(@RequestBody AiRequest request) {
        log.info("收到 AI 生成请求，提示词: {}", request.getPrompt());
        
        try {
            // 0. 从数据库加载 OpenAI 配置
            loadOpenAiConfigFromDatabase();
            
            // 1. 参数校验
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return Result.error(400, "提示词不能为空");
            }
            
            if (request.getPrompt().length() > 1000) {
                return Result.error(400, "提示词过长，最大支持 1000 字符");
            }
            
            // 2. 调用 OpenAI 生成 SQL
            AiResponse aiResponse = openAiService.generateSqlFromPrompt(request);
            
            if (!aiResponse.getSuccess()) {
                return Result.error(500, aiResponse.getErrorMessage());
            }
            
            log.info("AI 生成成功，表数量: {}", aiResponse.getTableCount());
            
            // 3. 解析 SQL 并生成 ER 图
            RenderOptions options = RenderOptions.builder()
                    .viewMode(com.coffeeviz.core.enums.ViewMode.PHYSICAL)
                    .direction(com.coffeeviz.core.enums.LayoutDirection.TB)
                    .showComments(true)
                    .build();
            
            ErService.ErResult erResult = erService.generateFromSql(aiResponse.getSqlDdl(), options);
            
            if (!erResult.isSuccess()) {
                return Result.error(500, "ER 图生成失败: " + erResult.getMessage());
            }
            
            // 4. 构建响应
            ErResponse response = new ErResponse();
            response.setMermaidCode(erResult.getMermaidCode());
            response.setSvgContent(erResult.getSvgContent());
            response.setPngBase64(erResult.getPngBase64());
            response.setWarnings(erResult.getWarnings());
            response.setTableCount(erResult.getTableCount());
            response.setRelationCount(erResult.getRelationCount());
            
            // 添加 AI 生成的额外信息
            response.setAiExplanation(aiResponse.getExplanation());
            response.setAiSuggestions(aiResponse.getSuggestions());
            response.setSqlDdl(aiResponse.getSqlDdl());
            
            log.info("AI 生成完成，表数量: {}, 关系数量: {}", 
                    erResult.getTableCount(), erResult.getRelationCount());
            
            return Result.success("AI 生成成功", response);
            
        } catch (Exception e) {
            log.error("AI 生成失败", e);
            return Result.error("AI 生成失败: " + e.getMessage());
        }
    }
    
    /**
     * AI 流式生成 SQL
     * 使用 SSE (Server-Sent Events) 实现流式输出
     * 流式输出完成后，自动解析 SQL 并生成 ER 图，通过 result 事件发送给前端
     * 注意：由于 EventSource 不支持自定义 headers，token 通过 URL 参数传递
     */
    @GetMapping("/generate/stream")
    public SseEmitter generateFromAiStream(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "dbType", defaultValue = "mysql") String dbType,
            @RequestParam(value = "namingStyle", defaultValue = "snake_case") String namingStyle,
            @RequestParam(value = "generateIndexes", defaultValue = "true") Boolean generateIndexes,
            @RequestParam(value = "generateComments", defaultValue = "true") Boolean generateComments,
            @RequestParam(value = "generateJunctionTables", defaultValue = "true") Boolean generateJunctionTables,
            @RequestParam(value = "token", required = false) String token) {
        
        log.info("收到流式 AI 生成请求，提示词: {}", prompt);
        
        // 创建 SSE Emitter，超时时间 3 分钟
        SseEmitter emitter = new SseEmitter(180000L);
        
        try {
            // 验证 token
            if (token == null || token.isEmpty()) {
                log.warn("流式请求缺少 token");
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"未登录或登录已过期\"}"));
                emitter.complete();
                return emitter;
            }
            
            Long userId;
            try {
                // 使用 token 进行登录验证
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    log.warn("Token 无效: {}", token);
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"message\":\"未登录或登录已过期\"}"));
                    emitter.complete();
                    return emitter;
                }
                userId = Long.parseLong(loginId.toString());
                log.info("流式请求验证成功，用户ID: {}", userId);
                
            } catch (Exception e) {
                log.warn("Token 验证失败: {}", e.getMessage());
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"未登录或登录已过期\"}"));
                emitter.complete();
                return emitter;
            }
            
            // 手动检查订阅权限（因为 SSE 端点无法使用 @RequireSubscription 注解）
            if (!subscriptionService.isSubscriptionValid(userId)) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"订阅已过期，请续费或升级订阅计划\"}"));
                emitter.complete();
                return emitter;
            }
            if (!subscriptionService.hasFeature(userId, "ai")) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"当前订阅计划不支持 AI 功能，请升级到 PRO 或 TEAM 计划\"}"));
                emitter.complete();
                return emitter;
            }
            
            // 手动检查配额（因为 SSE 端点无法使用 @RequireQuota 注解）
            if (!quotaService.checkQuota(userId, "ai_generate")) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"AI 生成配额不足，请升级订阅计划或等待配额重置\"}"));
                emitter.complete();
                return emitter;
            }
            
            // 从数据库加载 OpenAI 配置
            loadOpenAiConfigFromDatabase();
            
            // 参数校验
            if (prompt == null || prompt.trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"提示词不能为空\"}"));
                emitter.complete();
                return emitter;
            }
            
            if (prompt.length() > 1000) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"提示词过长，最大支持 1000 字符\"}"));
                emitter.complete();
                return emitter;
            }
            
            // 构建请求
            AiRequest request = AiRequest.builder()
                    .prompt(prompt)
                    .dbType(dbType)
                    .namingStyle(namingStyle)
                    .generateIndexes(generateIndexes)
                    .generateComments(generateComments)
                    .generateJunctionTables(generateJunctionTables)
                    .build();
            
            // 捕获 userId 用于回调中消耗配额
            final Long finalUserId = userId;
            
            // 调用流式生成（带完成回调）
            openAiService.generateSqlFromPromptStream(request, emitter, (fullContent) -> {
                try {
                    log.info("流式输出完成，开始后处理，内容长度: {}", fullContent.length());
                    
                    // 1. 解析完整内容提取 SQL DDL
                    String sqlDdl = extractSqlFromMarkdown(fullContent);
                    String explanation = extractExplanation(fullContent);
                    
                    if (sqlDdl == null || sqlDdl.isEmpty()) {
                        log.warn("未能从 AI 输出中提取 SQL DDL");
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(com.alibaba.fastjson2.JSON.toJSONString(java.util.Map.of(
                                        "success", false,
                                        "message", "未能从 AI 输出中提取 SQL DDL"
                                ))));
                        return;
                    }
                    
                    // 2. 生成 ER 图
                    com.coffeeviz.core.model.RenderOptions options = com.coffeeviz.core.model.RenderOptions.builder()
                            .viewMode(com.coffeeviz.core.enums.ViewMode.PHYSICAL)
                            .direction(com.coffeeviz.core.enums.LayoutDirection.TB)
                            .showComments(true)
                            .build();
                    
                    ErService.ErResult erResult = erService.generateFromSql(sqlDdl, options);
                    
                    if (!erResult.isSuccess()) {
                        log.warn("ER 图生成失败: {}", erResult.getMessage());
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(com.alibaba.fastjson2.JSON.toJSONString(java.util.Map.of(
                                        "success", false,
                                        "message", "ER 图生成失败: " + erResult.getMessage(),
                                        "sqlDdl", sqlDdl,
                                        "explanation", explanation
                                ))));
                        return;
                    }
                    
                    // 3. 构建结果并发送 result 事件
                    java.util.Map<String, Object> resultData = new java.util.LinkedHashMap<>();
                    resultData.put("success", true);
                    resultData.put("mermaidCode", erResult.getMermaidCode());
                    resultData.put("sqlDdl", sqlDdl);
                    resultData.put("tableCount", erResult.getTableCount());
                    resultData.put("relationCount", erResult.getRelationCount());
                    resultData.put("explanation", explanation);
                    
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(com.alibaba.fastjson2.JSON.toJSONString(resultData)));
                    
                    log.info("后处理完成，表数量: {}, 关系数量: {}", 
                            erResult.getTableCount(), erResult.getRelationCount());
                    
                    // 4. 消耗配额（成功后才消耗）
                    boolean used = quotaService.useQuota(finalUserId, "ai_generate");
                    if (!used) {
                        log.error("使用配额失败: userId={}", finalUserId);
                    }
                    
                } catch (Exception e) {
                    log.error("流式后处理失败", e);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(com.alibaba.fastjson2.JSON.toJSONString(java.util.Map.of(
                                        "success", false,
                                        "message", "后处理失败: " + e.getMessage()
                                ))));
                    } catch (Exception ex) {
                        log.error("发送后处理错误消息失败", ex);
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("流式 AI 生成失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"message\":\"" + e.getMessage() + "\"}"));
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
        
        return emitter;
    }
    
    /**
     * 检查 AI 功能是否可用
     */
    @GetMapping("/available")
    public Result<Boolean> checkAvailable() {
        try {
            // 从数据库加载配置
            loadOpenAiConfigFromDatabase();
            
            boolean available = openAiService.isAvailable();
            return Result.success(available);
        } catch (Exception e) {
            log.error("检查 AI 可用性失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 从 Markdown 中提取 SQL DDL
     */
    private String extractSqlFromMarkdown(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("```sql\\s*\\n(.*?)\\n```", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有 Markdown 标记，尝试提取所有 CREATE TABLE 语句
        pattern = java.util.regex.Pattern.compile("(CREATE TABLE.*?;)", java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);
        
        StringBuilder sql = new StringBuilder();
        while (matcher.find()) {
            sql.append(matcher.group(1)).append("\n\n");
        }
        
        return sql.toString().trim();
    }
    
    /**
     * 提取业务说明（移除 SQL 代码块后的文本）
     */
    private String extractExplanation(String text) {
        String cleaned = text.replaceAll("```sql.*?```", "").trim();
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:业务说明|设计思路|说明)[：:](.*)", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return cleaned.length() > 500 ? cleaned.substring(0, 500) + "..." : cleaned;
    }
    
    /**
     * 从数据库加载 OpenAI 配置
     */
    private void loadOpenAiConfigFromDatabase() {
        try {
            String apiKey = configService.getConfig("openai.api.key");
            String baseUrl = configService.getConfig("openai.api.base_url");
            String model = configService.getConfig("openai.model.name");
            
            if (apiKey != null && !apiKey.isEmpty()) {
                if (openAiService instanceof com.coffeeviz.llm.service.impl.OpenAiServiceImpl) {
                    com.coffeeviz.llm.service.impl.OpenAiServiceImpl impl = 
                            (com.coffeeviz.llm.service.impl.OpenAiServiceImpl) openAiService;
                    impl.setRuntimeApiKey(apiKey);
                    log.debug("从数据库加载 API Key");
                }
            }
            
            if (baseUrl != null && !baseUrl.isEmpty()) {
                if (openAiService instanceof com.coffeeviz.llm.service.impl.OpenAiServiceImpl) {
                    com.coffeeviz.llm.service.impl.OpenAiServiceImpl impl = 
                            (com.coffeeviz.llm.service.impl.OpenAiServiceImpl) openAiService;
                    impl.setRuntimeBaseUrl(baseUrl);
                    log.debug("从数据库加载 Base URL: {}", baseUrl);
                }
            }
            
            if (model != null && !model.isEmpty()) {
                if (openAiService instanceof com.coffeeviz.llm.service.impl.OpenAiServiceImpl) {
                    com.coffeeviz.llm.service.impl.OpenAiServiceImpl impl = 
                            (com.coffeeviz.llm.service.impl.OpenAiServiceImpl) openAiService;
                    impl.setRuntimeModel(model);
                    log.debug("从数据库加载模型: {}", model);
                }
            }
        } catch (Exception e) {
            log.warn("从数据库加载 OpenAI 配置失败，将使用配置文件中的默认值", e);
        }
    }
}
