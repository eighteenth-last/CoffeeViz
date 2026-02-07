package com.coffeeviz.dto;

import com.coffeeviz.core.enums.ViewMode;
import lombok.Data;

/**
 * SQL 解析请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class SqlParseRequest {
    
    /**
     * SQL 文本（必填）
     */
    private String sqlText;
    
    /**
     * 视图模式（可选，默认 PHYSICAL）
     */
    private ViewMode viewMode = ViewMode.PHYSICAL;
    
    /**
     * 是否推断隐式关系（可选，默认 false）
     */
    private Boolean inferRelations = false;
    
    /**
     * 表过滤（可选，逗号分隔）
     */
    private String tableFilter;
    
    /**
     * 关系深度（可选，默认 -1 表示无限制）
     */
    private Integer relationDepth = -1;
}
