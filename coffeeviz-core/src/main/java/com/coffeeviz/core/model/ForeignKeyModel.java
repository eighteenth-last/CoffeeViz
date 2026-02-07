package com.coffeeviz.core.model;

import com.coffeeviz.core.enums.RelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 外键模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyModel {
    
    /**
     * 外键名称
     */
    private String name;
    
    /**
     * 源表名
     */
    private String fromTable;
    
    /**
     * 源表列名列表
     */
    private List<String> fromColumns;
    
    /**
     * 目标表名
     */
    private String toTable;
    
    /**
     * 目标表列名列表
     */
    private List<String> toColumns;
    
    /**
     * 删除时的操作（CASCADE、SET NULL、RESTRICT 等）
     */
    private String onDelete;
    
    /**
     * 更新时的操作（CASCADE、SET NULL、RESTRICT 等）
     */
    private String onUpdate;
    
    /**
     * 关系类型（ONE_TO_ONE、ONE_TO_MANY、MANY_TO_MANY）
     */
    private RelationType relationType;
}
