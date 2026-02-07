package com.coffeeviz.dto;

import lombok.Data;

/**
 * 登录响应
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class LoginResponse {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String displayName;
        private String email;
        private String phone;
        private String avatarUrl;
    }
}
