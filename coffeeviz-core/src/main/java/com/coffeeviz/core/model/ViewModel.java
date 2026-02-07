package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 视图模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewModel {
    
    /**
     * 视图名称
     */
    private String name;
    
    /**
     * 视图注释
     */
    private String comment;
    
    /**
     * 列列表
     */
    @Builder.Default
    private List<ColumnModel> columns = new ArrayList<>();
    
    /**
     * 视图定义 SQL
     */
    private String definition;
}
