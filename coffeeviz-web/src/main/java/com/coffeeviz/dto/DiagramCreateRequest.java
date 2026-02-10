package com.coffeeviz.dto;

import lombok.Data;

/**
 * 创建架构图请求
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
public class DiagramCreateRequest {
    
    /**
     * 架构库 ID
     */
    private Long repositoryId;
    
    /**
     * 架构图名称
     */
    private String diagramName;
    
    /**
     * 架构图描述
     */
    private String description;
    
    /**
     * 来源类型（SQL/JDBC/AI）
     */
    private String sourceType;
    
    /**
     * 数据库类型（mysql/postgres）
     */
    private String dbType;
    
    /**
     * Mermaid 源码
     */
    private String mermaidCode;
    
    /**
     * SQL DDL 代码（AI 生成的原始 SQL）
     */
    private String sqlDdl;
    
    /**
     * PNG Base64 数据
     */
    private String pngBase64;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 关系数量
     */
    private Integer relationCount;
}
