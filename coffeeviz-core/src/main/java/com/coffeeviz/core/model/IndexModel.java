package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 索引模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexModel {
    
    /**
     * 索引名称
     */
    private String name;
    
    /**
     * 索引列名列表
     */
    private List<String> columns;
    
    /**
     * 是否唯一索引
     */
    private boolean unique;
    
    /**
     * 索引类型（BTREE、HASH 等）
     */
    private String indexType;
}
