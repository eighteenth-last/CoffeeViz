package com.coffeeviz.dto;

import lombok.Data;

/**
 * 登录请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
}
