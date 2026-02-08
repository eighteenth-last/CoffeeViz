package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 架构图实体类
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@TableName("biz_diagram")
public class Diagram implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
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
     * 图片 URL（MinIO 存储）
     */
    private String imageUrl;
    
    /**
     * 表数量
     */
    private Integer tableCount;
    
    /**
     * 关系数量
     */
    private Integer relationCount;
    
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
