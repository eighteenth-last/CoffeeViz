package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析结果
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseResult {
    
    /**
     * 数据库模型
     */
    private DatabaseModel databaseModel;
    
    /**
     * 警告信息列表
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    /**
     * 是否解析成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 创建成功的解析结果
     * 
     * @param databaseModel 数据库模型
     * @return 解析结果
     */
    public static ParseResult success(DatabaseModel databaseModel) {
        return ParseResult.builder()
                .databaseModel(databaseModel)
                .success(true)
                .message("解析成功")
                .build();
    }
    
    /**
     * 创建成功的解析结果（带警告）
     * 
     * @param databaseModel 数据库模型
     * @param warnings 警告信息列表
     * @return 解析结果
     */
    public static ParseResult success(DatabaseModel databaseModel, List<String> warnings) {
        return ParseResult.builder()
                .databaseModel(databaseModel)
                .warnings(warnings)
                .success(true)
                .message("解析成功")
                .build();
    }
    
    /**
     * 创建失败的解析结果
     * 
     * @param message 错误消息
     * @return 解析结果
     */
    public static ParseResult error(String message) {
        return ParseResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败的解析结果（带错误列表）
     * 
     * @param message 错误消息
     * @param errors 错误信息列表
     * @return 解析结果
     */
    public static ParseResult error(String message, List<String> errors) {
        return ParseResult.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
    
    /**
     * 添加警告信息
     * 
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }
    
    /**
     * 添加错误信息
     * 
     * @param error 错误信息
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}
