package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@TableName("biz_project")
public class Project implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
     * 项目名称
     */
    private String projectName;
    
    /**
     * 项目描述
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
     * 状态（DRAFT/PUBLISHED）
     */
    private String status;
    
    /**
     * Mermaid 源码
     */
    private String mermaidCode;
    
    /**
     * SVG 内容
     */
    private String svgContent;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
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
