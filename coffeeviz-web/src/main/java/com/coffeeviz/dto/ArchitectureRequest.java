package com.coffeeviz.dto;

import lombok.Data;

/**
 * 系统架构图生成请求
 *
 * @author CoffeeViz Team
 * @since 1.3.0
 */
@Data
public class ArchitectureRequest {

    /**
     * 输入模式: ddl / document / hybrid
     */
    private String mode = "document";

    /**
     * SQL DDL 文本（mode=ddl 或 hybrid 时使用）
     */
    private String sqlText;

    /**
     * 项目文档内容（Markdown 文本，mode=document 或 hybrid 时使用）
     */
    private String docContent;

    /**
     * 是否强制使用 AI（跳过规则提取）
     */
    private Boolean forceAi = false;
}
