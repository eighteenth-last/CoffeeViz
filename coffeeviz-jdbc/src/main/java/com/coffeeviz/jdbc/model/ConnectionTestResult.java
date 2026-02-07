package com.coffeeviz.jdbc.model;

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
     * 消息
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
     * 连接耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 创建成功的测试结果
     * 
     * @param message 消息
     * @param tableCount 表数量
     * @param dbVersion 数据库版本
     * @param duration 连接耗时
     * @return 测试结果
     */
    public static ConnectionTestResult success(String message, Integer tableCount, 
                                               String dbVersion, Long duration) {
        return ConnectionTestResult.builder()
                .success(true)
                .message(message)
                .tableCount(tableCount)
                .dbVersion(dbVersion)
                .duration(duration)
                .build();
    }
    
    /**
     * 创建失败的测试结果
     * 
     * @param message 错误消息
     * @return 测试结果
     */
    public static ConnectionTestResult failure(String message) {
        return ConnectionTestResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}
