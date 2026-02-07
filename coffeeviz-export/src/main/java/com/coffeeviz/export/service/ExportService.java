package com.coffeeviz.export.service;

import com.coffeeviz.export.exception.ExportException;

/**
 * 导出服务接口
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
public interface ExportService {
    
    /**
     * 导出 SVG
     * 
     * @param mermaidCode Mermaid 代码
     * @return SVG 内容（字节数组）
     * @throws ExportException 导出异常
     */
    byte[] exportSvg(String mermaidCode) throws ExportException;
    
    /**
     * 导出 PNG
     * 
     * @param mermaidCode Mermaid 代码
     * @param width 宽度
     * @param height 高度
     * @return PNG 内容（字节数组）
     * @throws ExportException 导出异常
     */
    byte[] exportPng(String mermaidCode, int width, int height) throws ExportException;
    
    /**
     * 导出 Mermaid 源码
     * 
     * @param mermaidCode Mermaid 代码
     * @return Mermaid 源码
     */
    String exportMermaid(String mermaidCode);
}
