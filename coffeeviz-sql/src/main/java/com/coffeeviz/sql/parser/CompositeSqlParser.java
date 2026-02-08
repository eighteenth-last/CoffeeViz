package com.coffeeviz.sql.parser;

import com.coffeeviz.core.model.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合 SQL 解析器
 * 实现多层解析策略：
 * L1: JSqlParser（精确解析）
 * L2: Druid SQL Parser（方言解析）
 * L3: 正则表达式（降级解析）
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Primary
@Component
public class CompositeSqlParser implements SqlParser {
    
    @Autowired
    private JSqlParserImpl jSqlParser;
    
    @Autowired
    private DruidSqlParserImpl druidSqlParser;
    
    @Autowired
    private RegexFallbackParser regexFallbackParser;
    
    @Override
    public ParseResult parse(String sqlText, String dialect) {
        log.info("开始多层解析策略，方言: {}", dialect);
        
        // 预处理 SQL：移除 MySQL 特殊语法
        String preprocessedSql = preprocessSql(sqlText);
        
        List<String> attemptLog = new ArrayList<>();
        
        // L1: 尝试使用 JSqlParser
        try {
            log.info("L1: 尝试使用 JSqlParser 解析");
            attemptLog.add("L1: 尝试 JSqlParser");
            ParseResult result = jSqlParser.parse(preprocessedSql, dialect);
            if (result.isSuccess()) {
                log.info("L1: JSqlParser 解析成功");
                result.getWarnings().add(0, "使用 JSqlParser 解析（L1 精确解析）");
                return result;
            }
            attemptLog.add("L1: JSqlParser 失败 - " + result.getMessage());
        } catch (Exception e) {
            log.debug("L1: JSqlParser 解析异常: {}", e.getMessage()); // 降级为 DEBUG
            attemptLog.add("L1: JSqlParser 异常 - " + e.getMessage());
        }
        
        // L2: 尝试使用 Druid SQL Parser
        try {
            log.info("L2: 尝试使用 Druid SQL Parser 解析");
            attemptLog.add("L2: 尝试 Druid SQL Parser");
            ParseResult result = druidSqlParser.parse(preprocessedSql, dialect);
            if (result.isSuccess()) {
                log.info("L2: Druid SQL Parser 解析成功");
                result.getWarnings().add(0, "使用 Druid SQL Parser 解析（L2 方言解析）");
                return result;
            }
            attemptLog.add("L2: Druid SQL Parser 失败 - " + result.getMessage());
        } catch (Exception e) {
            log.debug("L2: Druid SQL Parser 解析异常: {}", e.getMessage()); // 降级为 DEBUG
            attemptLog.add("L2: Druid SQL Parser 异常 - " + e.getMessage());
        }
        
        // L3: 使用正则表达式降级解析
        try {
            log.info("L3: 使用正则表达式降级解析");
            attemptLog.add("L3: 尝试正则表达式降级解析");
            ParseResult result = regexFallbackParser.parse(preprocessedSql, dialect);
            if (result.isSuccess()) {
                log.info("L3: 正则表达式解析成功");
                result.getWarnings().add(0, "使用正则表达式降级解析（L3）");
                result.getWarnings().addAll(attemptLog);
                return result;
            }
            attemptLog.add("L3: 正则表达式解析失败 - " + result.getMessage());
        } catch (Exception e) {
            log.error("L3: 正则表达式解析异常: {}", e.getMessage());
            attemptLog.add("L3: 正则表达式解析异常 - " + e.getMessage());
        }
        
        // 所有解析策略都失败
        log.error("所有解析策略都失败");
        String errorMessage = "所有解析策略都失败:\n" + String.join("\n", attemptLog);
        return ParseResult.error(errorMessage);
    }
    
    @Override
    public boolean supports(String dialect) {
        // 组合解析器支持所有方言
        return true;
    }
    
    /**
     * 预处理 SQL：移除 MySQL 特殊语法
     */
    private String preprocessSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }
        
        log.debug("开始预处理 SQL，原始长度: {}", sql.length());
        
        // 1. 移除 delimiter 命令（MySQL 客户端特殊命令）
        // 匹配 "delimiter ;;" 或 "delimiter ;" 等变体（不区分大小写）
        sql = sql.replaceAll("(?i)delimiter\\s+;;\\s*\n", "");
        sql = sql.replaceAll("(?i)delimiter\\s+;\\s*\n", "");
        sql = sql.replaceAll("(?i)delimiter\\s+;;\\s*$", "");
        sql = sql.replaceAll("(?i)delimiter\\s+;\\s*$", "");
        
        // 2. 移除触发器定义（保留 CREATE TRIGGER 语句本身，但移除 BEGIN...END 块）
        // 这样可以保留表结构，但跳过复杂的触发器逻辑
        sql = sql.replaceAll("(?is)DROP\\s+TRIGGER\\s+IF\\s+EXISTS\\s+`?\\w+`?\\s*;", "");
        sql = sql.replaceAll("(?is)delimiter\\s+;;.*?CREATE\\s+TRIGGER.*?END\\s*;;\\s*delimiter\\s*;", "");
        sql = sql.replaceAll("(?is)CREATE\\s+TRIGGER.*?END\\s*;;?", "");
        
        // 3. 替换 ;; 为 ;
        sql = sql.replaceAll(";;", ";");
        
        // 4. 移除多余的空行
        sql = sql.replaceAll("\n\\s*\n\\s*\n", "\n\n");
        
        log.debug("SQL 预处理完成，处理后长度: {}", sql.length());
        return sql;
    }
}
