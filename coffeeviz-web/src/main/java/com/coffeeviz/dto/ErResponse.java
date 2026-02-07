package com.coffeeviz.dto;

import lombok.Data;

import java.util.List;

/**
 * ER 图生成响应
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class ErResponse {
    
    /**
     * Mermaid 代码
     */
    private String mermaidCode;
    
    /**
     * SVG 内容
     */
    private String svgContent;
    
    /**
     * PNG Base64
     */
    private String pngBase64;
    
    /**
     * 警告信息
     */
    private List<String> warnings;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 关系数量
     */
    private Integer relationCount;
}
