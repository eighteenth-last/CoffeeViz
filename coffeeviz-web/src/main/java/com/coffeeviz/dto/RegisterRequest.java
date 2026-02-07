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
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
}
