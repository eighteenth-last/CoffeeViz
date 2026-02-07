package com.coffeeviz.dto;

import lombok.Data;

/**
 * 密码更新请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class PasswordUpdateRequest {
    
    /**
     * 旧密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
