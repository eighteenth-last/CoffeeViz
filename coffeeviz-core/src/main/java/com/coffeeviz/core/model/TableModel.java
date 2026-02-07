package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableModel {
    
    /**
     * 表名
     */
    private String name;
    
    /**
     * 表注释
     */
    private String comment;
    
    /**
     * 列列表
     */
    @Builder.Default
    private List<ColumnModel> columns = new ArrayList<>();
    
    /**
     * 主键
     */
    private PrimaryKeyModel primaryKey;
    
    /**
     * 外键列表
     */
    @Builder.Default
    private List<ForeignKeyModel> foreignKeys = new ArrayList<>();
    
    /**
     * 索引列表
     */
    @Builder.Default
    private List<IndexModel> indexes = new ArrayList<>();
    
    /**
     * 表类型（BASE TABLE、VIEW、JUNCTION 等）
     */
    private String tableType;
}
