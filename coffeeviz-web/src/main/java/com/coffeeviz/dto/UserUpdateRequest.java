package com.coffeeviz.dto;

import lombok.Data;

/**
 * 用户信息更新请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class UserUpdateRequest {
    
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
}
