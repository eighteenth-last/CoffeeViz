package com.coffeeviz.dto;

import lombok.Data;

/**
 * 项目创建请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class ProjectCreateRequest {
    
    /**
     * 项目名称（必填）
     */
    private String projectName;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * Mermaid 代码
     */
    private String mermaidCode;
    
    /**
     * 数据库类型
     */
    private String dbType;
    
    /**
     * 视图模式
     */
    private String viewMode;
}
