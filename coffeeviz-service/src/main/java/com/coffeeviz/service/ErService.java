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
    
    // 是否启用缓存（从配置文件读取）
    @org.springframework.beans.factory.annotation.Value("${coffeeviz.cache.enabled:false}")
    private boolean cacheEnabled;
    
    /**
     * 从 SQL 生成 ER 图
     * 
     * @param sqlText SQL 文本
     * @param options 渲染选项
     * @return ER 图结果
     */
    public ErResult generateFromSql(String sqlText, RenderOptions options) {
        long startTime = System.currentTimeMillis();
        log.info("开始从 SQL 生成 ER 图，SQL 长度: {}, 缓存状态: {}", sqlText.length(), cacheEnabled ? "启用" : "禁用");
        
        // 1. 尝试从缓存获取（仅当缓存启用时）
        ErResult cachedResult = null;
        String cacheKey = null;
        if (cacheEnabled) {
            cacheKey = generateCacheKey("sql", sqlText, options);
            cachedResult = getFromCache(cacheKey);
            if (cachedResult != null) {
                log.info("命中缓存: {}", cacheKey);
                return cachedResult;
            }
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
            
            // 5. 导出 PNG（Base64）- 根据表数量动态计算尺寸
            int[] dimensions = calculatePngDimensions(databaseModel.getTables().size());
            byte[] pngBytes = exportService.exportPng(mermaidCode, dimensions[0], dimensions[1]);
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
            
            // 6. 写入缓存（仅当缓存启用时）
            if (cacheEnabled && cacheKey != null) {
                saveToCache(cacheKey, result);
            }
            
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
        // 如果缓存未启用，直接返回 null
        if (!cacheEnabled || redisTemplate == null) {
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
        // 如果缓存未启用，不保存
        if (!cacheEnabled || redisTemplate == null) {
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
            
            // 4. 导出 PNG（Base64）- 根据表数量动态计算尺寸
            int[] dimensions = calculatePngDimensions(databaseModel.getTables().size());
            byte[] pngBytes = exportService.exportPng(mermaidCode, dimensions[0], dimensions[1]);
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
    
    /**
     * 根据表数量动态计算 PNG 导出尺寸
     * 表越多，需要的尺寸越大，以保证清晰度
     * 
     * @param tableCount 表数量
     * @return [width, height]
     */
    private int[] calculatePngDimensions(int tableCount) {
        int width, height;
        
        if (tableCount <= 5) {
            // 小型图表：1920x1080
            width = 1920;
            height = 1080;
        } else if (tableCount <= 10) {
            // 中型图表：2560x1440
            width = 2560;
            height = 1440;
        } else if (tableCount <= 20) {
            // 大型图表：3840x2160 (4K)
            width = 3840;
            height = 2160;
        } else if (tableCount <= 30) {
            // 超大型图表：5120x2880 (5K)
            width = 5120;
            height = 2880;
        } else {
            // 巨型图表：7680x4320 (8K)
            width = 7680;
            height = 4320;
        }
        
        log.debug("表数量: {}, 计算PNG尺寸: {}x{}", tableCount, width, height);
        return new int[]{width, height};
    }
}
