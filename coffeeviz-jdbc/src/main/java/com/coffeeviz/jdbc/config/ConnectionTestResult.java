package com.coffeeviz.jdbc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接测试结果
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResult {
    
    /**
     * 是否连接成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 数据库版本
     */
    private String dbVersion;
    
    /**
     * 创建成功的测试结果
     * 
     * @param tableCount 表数量
     * @param dbVersion 数据库版本
     * @return 测试结果
     */
    public static ConnectionTestResult success(Integer tableCount, String dbVersion) {
        return ConnectionTestResult.builder()
                .success(true)
                .message("连接成功")
                .tableCount(tableCount)
                .dbVersion(dbVersion)
                .build();
    }
    
    /**
     * 创建失败的测试结果
     * 
     * @param message 错误消息
     * @return 测试结果
     */
    public static ConnectionTestResult error(String message) {
        return ConnectionTestResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
