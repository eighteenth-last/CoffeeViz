package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目配置实体类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@TableName("biz_project_config")
public class ProjectConfig {
    
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
     * 配置类型（SQL/JDBC）
     */
    private String configType;
    
    /**
     * SQL 内容
     */
    private String sqlContent;
    
    /**
     * JDBC URL
     */
    private String jdbcUrl;
    
    /**
     * 数据库用户名
     */
    private String jdbcUsername;
    
    /**
     * 数据库密码（AES 加密）
     */
    private String jdbcPassword;
    
    /**
     * Schema 名称
     */
    private String schemaName;
    
    /**
     * 渲染选项（JSON 格式）
     */
    private String renderOptions;
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
