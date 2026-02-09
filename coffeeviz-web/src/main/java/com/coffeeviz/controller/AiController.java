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
