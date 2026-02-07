package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
@TableName("sys_user")
public class User {
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名（唯一）
     */
    private String username;
    
    /**
     * 密码（BCrypt 加密）
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * 职位头衔
     */
    private String jobTitle;
    
    /**
     * 头像 URL
     */
    private String avatarUrl;
    
    /**
     * 状态（1=正常，0=禁用）
     */
    private Integer status;
    
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
