package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主键模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryKeyModel {
    
    /**
     * 主键名称
     */
    private String name;
    
    /**
     * 主键列名列表
     */
    private List<String> columns;
}
