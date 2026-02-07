package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnModel {
    
    /**
     * 列名
     */
    private String name;
    
    /**
     * 规范化类型（如 VARCHAR、INT、BIGINT）
     */
    private String type;
    
    /**
     * 原始类型（保留数据库原始类型定义）
     */
    private String rawType;
    
    /**
     * 长度
     */
    private Integer length;
    
    /**
     * 精度（用于 DECIMAL 等类型）
     */
    private Integer precision;
    
    /**
     * 小数位数（用于 DECIMAL 等类型）
     */
    private Integer scale;
    
    /**
     * 是否可为空
     */
    private boolean nullable;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 列注释
     */
    private String comment;
    
    /**
     * 是否为主键的一部分
     */
    private boolean primaryKeyPart;
    
    /**
     * 是否自增
     */
    private boolean autoIncrement;
}
