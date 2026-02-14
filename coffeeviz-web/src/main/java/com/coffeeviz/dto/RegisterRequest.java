package com.coffeeviz.dto;

import lombok.Data;

/**
 * 注册请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱（必填）
     */
    private String email;
    
    /**
     * 邮箱验证码（必填）
     */
    private String emailCode;
    
    /**
     * 手机号
     */
    private String phone;
}
