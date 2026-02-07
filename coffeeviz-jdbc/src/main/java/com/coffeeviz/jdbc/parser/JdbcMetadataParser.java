package com.coffeeviz.jdbc.parser;

import com.coffeeviz.core.model.ParseResult;
import com.coffeeviz.jdbc.config.JdbcConfig;
import com.coffeeviz.jdbc.model.ConnectionTestResult;

/**
 * JDBC 元数据解析器接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public interface JdbcMetadataParser {
    
    /**
     * 从数据库连接读取元数据
     * 
     * @param config JDBC 配置
     * @return 解析结果
     */
    ParseResult parseFromDatabase(JdbcConfig config);
    
    /**
     * 测试数据库连接
     * 
     * @param config JDBC 配置
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(JdbcConfig config);
}
