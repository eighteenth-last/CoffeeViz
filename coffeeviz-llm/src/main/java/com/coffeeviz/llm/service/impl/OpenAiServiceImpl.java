package com.coffeeviz.llm.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.coffeeviz.llm.config.OpenAiConfig;
import com.coffeeviz.llm.model.AiRequest;
import com.coffeeviz.llm.model.AiResponse;
import com.coffeeviz.llm.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI 服务实现
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class OpenAiServiceImpl implements OpenAiService {
    
    @Autowired
    private OpenAiConfig openAiConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    // 运行时配置（优先级高于 openAiConfig）
    private String runtimeApiKey;
    private String runtimeBaseUrl;
    private String runtimeModel;
    
    /**
     * 设置运行时 API Key（用于从数据库动态加载配置）
     */
    public void setRuntimeApiKey(String apiKey) {
        this.runtimeApiKey = apiKey;
    }
    
    /**
     * 设置运行时 Base URL（用于从数据库动态加载配置）
     */
    public void setRuntimeBaseUrl(String baseUrl) {
        this.runtimeBaseUrl = baseUrl;
    }
    
    /**
     * 设置运行时模型名称（用于从数据库动态加载配置）
     */
    public void setRuntimeModel(String model) {
        this.runtimeModel = model;
    }
    
    @Override
    public AiResponse generateSqlFromPrompt(AiRequest request) {
        log.info("开始 AI 生成 SQL，提示词: {}", request.getPrompt());
        
        try {
            // 1. 检查配置
            if (!isAvailable()) {
                return AiResponse.builder()
                        .success(false)
                        .errorMessage("OpenAI API 未配置，请在数据库 sys_config 表中设置 openai.api.key")
                        .build();
            }
            
            // 2. 构建系统提示词
            String systemPrompt = buildSystemPrompt(request);
            
            // 3. 构建用户提示词
            String userPrompt = buildUserPrompt(request);
            
            // 4. 调用 OpenAI API
            String response = callOpenAiApi(systemPrompt, userPrompt);
            
            // 5. 解析响应
            return parseAiResponse(response);
            
        } catch (Exception e) {
            log.error("AI 生成失败", e);
            return AiResponse.builder()
                    .success(false)
                    .errorMessage("AI 生成失败: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public boolean isAvailable() {
        String apiKey = getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * 获取 API Key（优先使用运行时配置）
     */
    private String getApiKey() {
        if (runtimeApiKey != null && !runtimeApiKey.isEmpty()) {
            return runtimeApiKey;
        }
        return openAiConfig.getKey();
    }
    
    /**
     * 获取 Base URL（优先使用运行时配置）
     */
    private String getBaseUrl() {
        if (runtimeBaseUrl != null && !runtimeBaseUrl.isEmpty()) {
            return runtimeBaseUrl;
        }
        return openAiConfig.getBaseUrl();
    }
    
    /**
     * 获取模型名称（优先使用运行时配置）
     */
    private String getModelName() {
        if (runtimeModel != null && !runtimeModel.isEmpty()) {
            return runtimeModel;
        }
        return openAiConfig.getModel();
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(AiRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的数据库架构师，擅长根据业务需求设计规范化的数据库模型。\n\n");
        sb.append("要求：\n");
        sb.append("1. 生成标准的 ").append(request.getDbType().toUpperCase()).append(" DDL 语句\n");
        sb.append("2. 使用 ").append(request.getNamingStyle()).append(" 命名风格\n");
        sb.append("3. 遵循数据库设计范式（至少 3NF）\n");
        
        if (request.getGenerateJunctionTables()) {
            sb.append("4. 对于多对多关系，创建中间表\n");
        }
        
        if (request.getGenerateIndexes()) {
            sb.append("5. 为外键和常用查询字段添加索引\n");
        }
        
        if (request.getGenerateComments()) {
            sb.append("6. 为表和字段添加清晰的注释\n");
        }
        
        sb.append("\n请按以下格式返回：\n");
        sb.append("```sql\n");
        sb.append("-- 这里是生成的 SQL DDL\n");
        sb.append("```\n\n");
        sb.append("业务说明：\n");
        sb.append("简要说明设计思路和表关系\n");
        
        return sb.toString();
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(AiRequest request) {
        return "请根据以下业务需求设计数据库模型：\n\n" + request.getPrompt();
    }
    
    /**
     * 调用 OpenAI API
     */
    private String callOpenAiApi(String systemPrompt, String userPrompt) {
        String baseUrl = getBaseUrl();
        String url = baseUrl + "/chat/completions";
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", getModelName());
        requestBody.put("max_tokens", openAiConfig.getMaxTokens());
        requestBody.put("temperature", openAiConfig.getTemperature());
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getApiKey());
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
        
        // 发送请求
        log.info("调用 OpenAI API: {}, model: {}", url, getModelName());
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject responseBody = JSON.parseObject(response.getBody());
            JSONArray choices = responseBody.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content");
            }
        }
        
        throw new RuntimeException("OpenAI API 调用失败: " + response.getStatusCode());
    }
    
    /**
     * 解析 AI 响应
     */
    private AiResponse parseAiResponse(String response) {
        log.info("解析 AI 响应，长度: {}", response.length());
        
        // 提取 SQL DDL（在 ```sql 和 ``` 之间）
        String sqlDdl = extractSqlFromMarkdown(response);
        
        // 提取业务说明
        String explanation = extractExplanation(response);
        
        // 统计表数量
        int tableCount = countTables(sqlDdl);
        
        // 提取建议
        List<String> suggestions = extractSuggestions(response);
        
        return AiResponse.builder()
                .sqlDdl(sqlDdl)
                .explanation(explanation)
                .tableCount(tableCount)
                .suggestions(suggestions)
                .success(true)
                .build();
    }
    
    /**
     * 从 Markdown 中提取 SQL
     */
    private String extractSqlFromMarkdown(String text) {
        Pattern pattern = Pattern.compile("```sql\\s*\\n(.*?)\\n```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有 Markdown 标记，尝试提取所有 CREATE TABLE 语句
        pattern = Pattern.compile("(CREATE TABLE.*?;)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);
        
        StringBuilder sql = new StringBuilder();
        while (matcher.find()) {
            sql.append(matcher.group(1)).append("\n\n");
        }
        
        return sql.toString().trim();
    }
    
    /**
     * 提取业务说明
     */
    private String extractExplanation(String text) {
        // 移除 SQL 代码块
        String cleaned = text.replaceAll("```sql.*?```", "").trim();
        
        // 查找"业务说明"或"设计思路"等关键词后的内容
        Pattern pattern = Pattern.compile("(?:业务说明|设计思路|说明)[：:](.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cleaned);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有找到，返回清理后的文本
        return cleaned.length() > 500 ? cleaned.substring(0, 500) + "..." : cleaned;
    }
    
    /**
     * 统计表数量
     */
    private int countTables(String sql) {
        Pattern pattern = Pattern.compile("CREATE TABLE", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        return count;
    }
    
    /**
     * 提取建议
     */
    private List<String> extractSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();
        
        // 查找列表项（- 或 1. 开头）
        Pattern pattern = Pattern.compile("^[\\-\\*]\\s+(.+)$|^\\d+\\.\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find() && suggestions.size() < 5) {
            String suggestion = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (suggestion != null && !suggestion.isEmpty()) {
                suggestions.add(suggestion.trim());
            }
        }
        
        return suggestions;
    }
}
