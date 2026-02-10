package com.coffeeviz.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 加入团队请求（用于新用户注册并加入）
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class JoinTeamRequest {
    
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
    
    // 以下字段用于新用户注册
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String displayName;
}
