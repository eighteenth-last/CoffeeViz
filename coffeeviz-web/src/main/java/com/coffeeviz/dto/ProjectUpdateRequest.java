package com.coffeeviz.dto;

import lombok.Data;

/**
 * 项目更新请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class ProjectUpdateRequest {
    
    /**
     * 项目 ID（必填）
     */
    private Long id;
    
    /**
     * 项目名称
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
     * PNG Base64 数据
     */
    private String pngBase64;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 是否创建新版本
     */
    private Boolean createVersion = false;
    
    /**
     * 变更日志
     */
    private String changeLog;
}
