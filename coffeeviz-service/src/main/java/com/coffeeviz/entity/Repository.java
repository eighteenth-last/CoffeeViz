package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 架构库实体类
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@TableName("biz_repository")
public class Repository implements Serializable {
    
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
     * 架构库名称
     */
    private String repositoryName;
    
    /**
     * 架构库描述
     */
    private String description;
    
    /**
     * 架构图数量
     */
    private Integer diagramCount;
    
    /**
     * 状态（active/archived）
     */
    private String status;
    
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
