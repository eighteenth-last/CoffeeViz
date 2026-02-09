package com.coffeeviz.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    
    /**
     * 用户提示词
     */
    private String prompt;
    
    /**
     * 数据库类型（mysql/postgres）
     */
    @Builder.Default
    private String dbType = "mysql";
    
    /**
     * 命名风格（snake_case/camelCase）
     */
    @Builder.Default
    private String namingStyle = "snake_case";
    
    /**
     * 是否生成中间表
     */
    @Builder.Default
    private Boolean generateJunctionTables = true;
    
    /**
     * 是否生成索引建议
     */
    @Builder.Default
    private Boolean generateIndexes = true;
    
    /**
     * 是否生成注释
     */
    @Builder.Default
    private Boolean generateComments = true;
}
