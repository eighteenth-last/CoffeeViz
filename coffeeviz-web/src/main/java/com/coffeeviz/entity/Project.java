package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目实体类（临时映射到 biz_repository 表以保持兼容）
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 * @deprecated 请使用 Repository 和 Diagram 实体
 */
@Data
@TableName("biz_repository")
public class Project {
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 项目名称（映射到 repository_name）
     */
    @TableField("repository_name")
    private String projectName;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 来源类型（SQL/JDBC/AI）- 临时字段，实际存在 diagram 表
     */
    @TableField(exist = false)
    private String sourceType;
    
    /**
     * 数据库类型（mysql/postgres）- 临时字段，实际存在 diagram 表
     */
    @TableField(exist = false)
    private String dbType;
    
    /**
     * 状态（active/archived）
     */
    private String status;
    
    /**
     * Mermaid 源码 - 临时字段，实际存在 diagram 表
     */
    @TableField(exist = false)
    private String mermaidCode;
    
    /**
     * SVG 内容 - 临时字段，不再使用
     */
    @TableField(exist = false)
    private String svgContent;
    
    /**
     * 图片 URL - 临时字段，实际存在 diagram 表
     */
    @TableField(exist = false)
    private String imageUrl;
    
    /**
     * 表数量 - 临时字段，实际存在 diagram 表
     */
    @TableField(exist = false)
    private Integer tableCount;
    
    /**
     * 架构图数量（实际字段 diagram_count）
     */
    private Integer diagramCount;
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
