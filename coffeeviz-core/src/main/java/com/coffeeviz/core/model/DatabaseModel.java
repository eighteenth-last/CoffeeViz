package com.coffeeviz.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库模型
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseModel {
    
    /**
     * 数据库类型（mysql、postgres 等）
     */
    private String dbType;
    
    /**
     * 数据库/Schema 名称
     */
    private String schemaName;
    
    /**
     * 表列表
     */
    @Builder.Default
    private List<TableModel> tables = new ArrayList<>();
    
    /**
     * 视图列表（可选）
     */
    @Builder.Default
    private List<ViewModel> views = new ArrayList<>();
    
    /**
     * 额外元数据（用于存储数据库版本、字符集等信息）
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}
