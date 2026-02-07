package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.annotation.RateLimit;
import com.coffeeviz.common.Result;
import com.coffeeviz.core.enums.LayoutDirection;
import com.coffeeviz.core.model.RenderOptions;
import com.coffeeviz.dto.ErResponse;
import com.coffeeviz.dto.JdbcConnectRequest;
import com.coffeeviz.dto.SqlParseRequest;
import com.coffeeviz.jdbc.config.JdbcConfig;
import com.coffeeviz.jdbc.model.ConnectionTestResult;
import com.coffeeviz.jdbc.parser.JdbcMetadataParser;
import com.coffeeviz.service.ErService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;

/**
 * ER 图生成 Controller
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/er")
public class ErController {
    
    @Autowired
    private ErService erService;
    
    @Autowired
    private JdbcMetadataParser jdbcMetadataParser;
    
    /**
     * 从 SQL 解析生成 ER 图
     */
    @PostMapping("/parse-sql")
    @RateLimit(key = "parse_sql", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<ErResponse> parseSql(@RequestBody SqlParseRequest request) {
        log.info("收到 SQL 解析请求，SQL 长度: {}", 
                request.getSqlText() != null ? request.getSqlText().length() : 0);
        
        try {
            // 1. 参数校验
            if (request.getSqlText() == null || request.getSqlText().trim().isEmpty()) {
                return Result.error(400, "SQL 文本不能为空");
            }
            
            if (request.getSqlText().length() > 1_000_000) {
                return Result.error(400, "SQL 文本过长，最大支持 1MB");
            }
            
            // 2. 构建渲染选项
            RenderOptions options = buildRenderOptions(request);
            
            // 3. 调用服务生成 ER 图
            ErService.ErResult result = erService.generateFromSql(request.getSqlText(), options);
            
            if (!result.isSuccess()) {
                return Result.error(500, result.getMessage());
            }
            
            // 4. 构建响应
            ErResponse response = new ErResponse();
            response.setMermaidCode(result.getMermaidCode());
            response.setSvgContent(result.getSvgContent());
            response.setPngBase64(result.getPngBase64());
            response.setWarnings(result.getWarnings());
            response.setTableCount(result.getTableCount());
            response.setRelationCount(result.getRelationCount());
            
            log.info("SQL 解析成功，表数量: {}, 关系数量: {}", 
                    result.getTableCount(), result.getRelationCount());
            
            return Result.success("解析成功", response);
            
        } catch (Exception e) {
            log.error("SQL 解析失败", e);
            return Result.error("解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 从 JDBC 连接生成 ER 图
     */
    @PostMapping("/connect-jdbc")
    @RateLimit(key = "connect_jdbc", time = 60, count = 10, limitType = RateLimit.LimitType.USER)
    public Result<ErResponse> connectJdbc(@RequestBody JdbcConnectRequest request) {
        log.info("收到 JDBC 连接请求，数据库类型: {}, URL: {}", 
                request.getDbType(), request.getJdbcUrl());
        
        try {
            // 1. 参数校验
            if (request.getDbType() == null || request.getDbType().trim().isEmpty()) {
                return Result.error(400, "数据库类型不能为空");
            }
            
            if (request.getJdbcUrl() == null || request.getJdbcUrl().trim().isEmpty()) {
                return Result.error(400, "JDBC URL 不能为空");
            }
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            
            if (request.getPassword() == null) {
                return Result.error(400, "密码不能为空");
            }
            
            // 2. 构建 JDBC 配置
            JdbcConfig jdbcConfig = new JdbcConfig();
            jdbcConfig.setDbType(request.getDbType());
            jdbcConfig.setJdbcUrl(request.getJdbcUrl());
            jdbcConfig.setUsername(request.getUsername());
            jdbcConfig.setPassword(request.getPassword());
            jdbcConfig.setSchemaName(request.getSchemaName());
            jdbcConfig.setReadOnly(true);
            jdbcConfig.setTimeout(30);
            
            // 3. 构建渲染选项
            RenderOptions options = buildRenderOptions(request);
            
            // 4. 调用服务生成 ER 图
            ErService.ErResult result = erService.generateFromJdbc(jdbcConfig, options);
            
            if (!result.isSuccess()) {
                return Result.error(500, result.getMessage());
            }
            
            // 5. 构建响应
            ErResponse response = new ErResponse();
            response.setMermaidCode(result.getMermaidCode());
            response.setSvgContent(result.getSvgContent());
            response.setPngBase64(result.getPngBase64());
            response.setWarnings(result.getWarnings());
            response.setTableCount(result.getTableCount());
            response.setRelationCount(result.getRelationCount());
            
            log.info("JDBC 连接成功，表数量: {}, 关系数量: {}", 
                    result.getTableCount(), result.getRelationCount());
            
            return Result.success("连接成功", response);
            
        } catch (Exception e) {
            log.error("JDBC 连接失败", e);
            return Result.error("连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试 JDBC 连接
     */
    @PostMapping("/test-connection")
    @RateLimit(key = "test_connection", time = 60, count = 20, limitType = RateLimit.LimitType.USER)
    public Result<String> testConnection(@RequestBody JdbcConnectRequest request) {
        log.info("收到测试连接请求，数据库类型: {}, URL: {}", 
                request.getDbType(), request.getJdbcUrl());
        
        try {
            // 1. 参数校验
            if (request.getDbType() == null || request.getDbType().trim().isEmpty()) {
                return Result.error(400, "数据库类型不能为空");
            }
            
            if (request.getJdbcUrl() == null || request.getJdbcUrl().trim().isEmpty()) {
                return Result.error(400, "JDBC URL 不能为空");
            }
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            
            if (request.getPassword() == null) {
                return Result.error(400, "密码不能为空");
            }
            
            // 2. 构建 JDBC 配置
            JdbcConfig jdbcConfig = new JdbcConfig();
            jdbcConfig.setDbType(request.getDbType());
            jdbcConfig.setJdbcUrl(request.getJdbcUrl());
            jdbcConfig.setUsername(request.getUsername());
            jdbcConfig.setPassword(request.getPassword());
            jdbcConfig.setSchemaName(request.getSchemaName());
            jdbcConfig.setReadOnly(true);
            jdbcConfig.setTimeout(10);
            
            // 3. 测试连接
            ConnectionTestResult testResult = jdbcMetadataParser.testConnection(jdbcConfig);
            
            if (testResult.isSuccess()) {
                log.info("连接测试成功: {}", testResult.getMessage());
                return Result.success("连接测试成功", testResult.getMessage());
            } else {
                log.warn("连接测试失败: {}", testResult.getMessage());
                return Result.error(500, testResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("连接测试失败", e);
            return Result.error("连接测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建渲染选项（从 SqlParseRequest）
     */
    private RenderOptions buildRenderOptions(SqlParseRequest request) {
        RenderOptions options = new RenderOptions();
        options.setViewMode(request.getViewMode());
        options.setDirection(LayoutDirection.TB);
        
        if (request.getTableFilter() != null && !request.getTableFilter().trim().isEmpty()) {
            options.setIncludeTables(new HashSet<>(Arrays.asList(request.getTableFilter().split(","))));
        }
        
        if (request.getRelationDepth() != null) {
            options.setRelationDepth(request.getRelationDepth());
        }
        
        return options;
    }
    
    /**
     * 构建渲染选项（从 JdbcConnectRequest）
     */
    private RenderOptions buildRenderOptions(JdbcConnectRequest request) {
        RenderOptions options = new RenderOptions();
        options.setViewMode(request.getViewMode());
        options.setDirection(LayoutDirection.TB);
        
        if (request.getTableFilter() != null && !request.getTableFilter().trim().isEmpty()) {
            options.setIncludeTables(new HashSet<>(Arrays.asList(request.getTableFilter().split(","))));
        }
        
        if (request.getRelationDepth() != null) {
            options.setRelationDepth(request.getRelationDepth());
        }
        
        return options;
    }
}
