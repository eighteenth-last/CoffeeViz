package com.coffeeviz.service;

import com.alibaba.fastjson2.JSON;
import com.coffeeviz.core.model.DatabaseModel;
import com.coffeeviz.core.model.ParseResult;
import com.coffeeviz.core.model.RenderOptions;
import com.coffeeviz.core.renderer.MermaidRenderer;
import com.coffeeviz.export.service.ExportService;
import com.coffeeviz.jdbc.config.JdbcConfig;
import com.coffeeviz.jdbc.parser.JdbcMetadataParser;
import com.coffeeviz.sql.parser.SqlParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * ER 图生成服务
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErService {
    
    private static final String CACHE_KEY_PREFIX = "er:diagram:";
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 1 小时
    
    @Autowired
    private SqlParser sqlParser;
    
    @Autowired
    private JdbcMetadataParser jdbcMetadataParser;
    
    @Autowired
    private MermaidRenderer mermaidRenderer;
    
    @Autowired
    private ExportService exportService;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 从 SQL 生成 ER 图
     * 
     * @param sqlText SQL 文本
     * @param options 渲染选项
     * @return ER 图结果
     */
    public ErResult generateFromSql(String sqlText, RenderOptions options) {
        long startTime = System.currentTimeMillis();
        log.info("开始从 SQL 生成 ER 图，SQL 长度: {}", sqlText.length());
        
        // 1. 尝试从缓存获取
        String cacheKey = generateCacheKey("sql", sqlText, options);
        ErResult cachedResult = getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("命中缓存: {}", cacheKey);
            return cachedResult;
        }
        
        try {
            // 2. 解析 SQL
            ParseResult parseResult = sqlParser.parse(sqlText, "auto");
            
            if (!parseResult.isSuccess()) {
                log.warn("SQL 解析失败: {}", parseResult.getMessage());
                return ErResult.error(parseResult.getMessage());
            }
            
            DatabaseModel databaseModel = parseResult.getDatabaseModel();
            
            // 3. 渲染 Mermaid 代码
            String mermaidCode = mermaidRenderer.render(databaseModel, options);
            
            // 4. 导出 SVG
            byte[] svgBytes = exportService.exportSvg(mermaidCode);
            String svgContent = new String(svgBytes);
            
            // 5. 导出 PNG（Base64）
            byte[] pngBytes = exportService.exportPng(mermaidCode, 1920, 1080);
            String pngBase64 = "data:image/png;base64," + 
                java.util.Base64.getEncoder().encodeToString(pngBytes);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("SQL 解析成功，耗时: {}ms, 表数量: {}, 关系数量: {}", 
                    duration, 
                    databaseModel.getTables().size(),
                    databaseModel.getTables().stream()
                        .mapToInt(t -> t.getForeignKeys().size())
                        .sum());
            
            ErResult result = ErResult.success(
                mermaidCode,
                svgContent,
                pngBase64,
                parseResult.getWarnings(),
                databaseModel.getTables().size(),
                databaseModel.getTables().stream()
                    .mapToInt(t -> t.getForeignKeys().size())
                    .sum()
            );
            
            // 6. 写入缓存
            saveToCache(cacheKey, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("从 SQL 生成 ER 图失败", e);
            return ErResult.error("生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(String type, String content, RenderOptions options) {
        String combined = type + ":" + content + ":" + JSON.toJSONString(options);
        String hash = DigestUtils.md5DigestAsHex(combined.getBytes(StandardCharsets.UTF_8));
        return CACHE_KEY_PREFIX + hash;
    }
    
    /**
     * 从缓存获取
     */
    private ErResult getFromCache(String cacheKey) {
        if (redisTemplate == null) {
            return null;
        }
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof ErResult) {
                return (ErResult) cached;
            }
        } catch (Exception e) {
            log.warn("从缓存读取失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 保存到缓存
     */
    private void saveToCache(String cacheKey, ErResult result) {
        if (redisTemplate == null) {
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.debug("结果已缓存: {}", cacheKey);
        } catch (Exception e) {
            log.warn("写入缓存失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从 JDBC 连接生成 ER 图
     * 
     * @param jdbcConfig JDBC 配置
     * @param options 渲染选项
     * @return ER 图结果
     */
    public ErResult generateFromJdbc(JdbcConfig jdbcConfig, RenderOptions options) {
        long startTime = System.currentTimeMillis();
        log.info("开始从 JDBC 生成 ER 图，数据库类型: {}, URL: {}", 
                jdbcConfig.getDbType(), jdbcConfig.getJdbcUrl());
        
        try {
            // 1. 从数据库读取元数据
            ParseResult parseResult = jdbcMetadataParser.parseFromDatabase(jdbcConfig);
            
            if (!parseResult.isSuccess()) {
                log.warn("JDBC 元数据解析失败: {}", parseResult.getMessage());
                return ErResult.error(parseResult.getMessage());
            }
            
            DatabaseModel databaseModel = parseResult.getDatabaseModel();
            
            // 2. 渲染 Mermaid 代码
            String mermaidCode = mermaidRenderer.render(databaseModel, options);
            
            // 3. 导出 SVG
            byte[] svgBytes = exportService.exportSvg(mermaidCode);
            String svgContent = new String(svgBytes);
            
            // 4. 导出 PNG（Base64）
            byte[] pngBytes = exportService.exportPng(mermaidCode, 1920, 1080);
            String pngBase64 = "data:image/png;base64," + 
                java.util.Base64.getEncoder().encodeToString(pngBytes);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("JDBC 解析成功，耗时: {}ms, 表数量: {}, 关系数量: {}", 
                    duration, 
                    databaseModel.getTables().size(),
                    databaseModel.getTables().stream()
                        .mapToInt(t -> t.getForeignKeys().size())
                        .sum());
            
            return ErResult.success(
                mermaidCode,
                svgContent,
                pngBase64,
                parseResult.getWarnings(),
                databaseModel.getTables().size(),
                databaseModel.getTables().stream()
                    .mapToInt(t -> t.getForeignKeys().size())
                    .sum()
            );
            
        } catch (Exception e) {
            log.error("从 JDBC 生成 ER 图失败", e);
            return ErResult.error("生成失败: " + e.getMessage());
        }
    }
    
    /**
     * ER 图生成结果
     */
    public static class ErResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private boolean success;
        private String message;
        private String mermaidCode;
        private String svgContent;
        private String pngBase64;
        private java.util.List<String> warnings;
        private int tableCount;
        private int relationCount;
        
        public static ErResult success(String mermaidCode, String svgContent, 
                                       String pngBase64, java.util.List<String> warnings,
                                       int tableCount, int relationCount) {
            ErResult result = new ErResult();
            result.success = true;
            result.message = "生成成功";
            result.mermaidCode = mermaidCode;
            result.svgContent = svgContent;
            result.pngBase64 = pngBase64;
            result.warnings = warnings;
            result.tableCount = tableCount;
            result.relationCount = relationCount;
            return result;
        }
        
        public static ErResult error(String message) {
            ErResult result = new ErResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getMermaidCode() { return mermaidCode; }
        public String getSvgContent() { return svgContent; }
        public String getPngBase64() { return pngBase64; }
        public java.util.List<String> getWarnings() { return warnings; }
        public int getTableCount() { return tableCount; }
        public int getRelationCount() { return relationCount; }
    }
    
    /**
     * 异步从 SQL 生成 ER 图（用于大规模数据库）
     * 
     * @param sqlText SQL 文本
     * @param options 渲染选项
     * @return CompletableFuture
     */
    @org.springframework.scheduling.annotation.Async("erTaskExecutor")
    public java.util.concurrent.CompletableFuture<ErResult> generateFromSqlAsync(String sqlText, RenderOptions options) {
        log.info("异步任务开始: 从 SQL 生成 ER 图");
        try {
            ErResult result = generateFromSql(sqlText, options);
            return java.util.concurrent.CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步任务失败", e);
            return java.util.concurrent.CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 异步从 JDBC 生成 ER 图（用于大规模数据库）
     * 
     * @param jdbcConfig JDBC 配置
     * @param options 渲染选项
     * @return CompletableFuture
     */
    @org.springframework.scheduling.annotation.Async("erTaskExecutor")
    public java.util.concurrent.CompletableFuture<ErResult> generateFromJdbcAsync(JdbcConfig jdbcConfig, RenderOptions options) {
        log.info("异步任务开始: 从 JDBC 生成 ER 图");
        try {
            ErResult result = generateFromJdbc(jdbcConfig, options);
            return java.util.concurrent.CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("异步任务失败", e);
            return java.util.concurrent.CompletableFuture.failedFuture(e);
        }
    }
}
