package com.coffeeviz.jdbc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JDBC 配置
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcConfig {
    
    /**
     * 数据库类型（mysql、postgres）
     */
    private String dbType;
    
    /**
     * JDBC URL
     */
    private String jdbcUrl;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * Schema 名称（可选）
     */
    private String schemaName;
    
    /**
     * 连接超时（秒），默认 10 秒
     */
    @Builder.Default
    private Integer timeout = 10;
    
    /**
     * 是否只读模式，默认 true
     */
    @Builder.Default
    private boolean readOnly = true;
}
