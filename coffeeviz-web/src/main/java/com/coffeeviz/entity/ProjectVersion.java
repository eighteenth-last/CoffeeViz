package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目版本实体类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@TableName("biz_project_version")
public class ProjectVersion {
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 项目 ID
     */
    private Long projectId;
    
    /**
     * 版本号
     */
    private Integer versionNo;
    
    /**
     * Mermaid 源码
     */
    private String mermaidCode;
    
    /**
     * 变更日志
     */
    private String changeLog;
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
