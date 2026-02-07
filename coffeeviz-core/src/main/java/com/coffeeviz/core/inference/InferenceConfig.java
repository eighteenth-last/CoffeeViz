package com.coffeeviz.core.inference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关系推断配置
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceConfig {
    
    /**
     * 是否启用命名约定推断
     * 例如：user_id 字段推断为指向 users 表的外键
     */
    @Builder.Default
    private boolean enableNamingConvention = false;
    
    /**
     * 是否识别中间表（N:M 关系）
     */
    @Builder.Default
    private boolean identifyJunctionTables = true;
    
    /**
     * 命名约定后缀（默认为 _id）
     */
    @Builder.Default
    private String namingConventionSuffix = "_id";
}
