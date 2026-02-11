package com.coffeeviz.dto;

import lombok.Data;

import java.util.List;

/**
 * 系统功能结构图生成响应
 *
 * @author CoffeeViz Team
 * @since 1.3.0
 */
@Data
public class ArchitectureResponse {

    /**
     * Mermaid graph TD 代码
     */
    private String mermaidCode;

    /**
     * 树形结构数据
     */
    private TreeNodeDTO tree;

    /**
     * 警告信息
     */
    private List<String> warnings;

    /**
     * 提取方式: rule / ai / hybrid
     */
    private String extractMethod;

    /**
     * 总节点数
     */
    private Integer nodeCount;

    /**
     * 树形节点 DTO
     */
    @Data
    public static class TreeNodeDTO {
        private String id;
        private String name;
        private int level;
        private List<TreeNodeDTO> children;
    }
}
