package com.coffeeviz.sql.parser;

import com.coffeeviz.core.model.ParseResult;

/**
 * SQL DDL 解析器接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public interface SqlParser {
    
    /**
     * 解析 SQL DDL 为 SchemaModel
     * 
     * @param sqlText SQL 文本
     * @param dialect 方言（mysql/postgres/auto）
     * @return 解析结果
     */
    ParseResult parse(String sqlText, String dialect);
    
    /**
     * 检查是否支持指定方言
     * 
     * @param dialect 方言
     * @return 是否支持
     */
    boolean supports(String dialect);
}
