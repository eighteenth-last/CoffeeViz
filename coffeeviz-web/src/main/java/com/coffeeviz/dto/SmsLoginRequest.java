package com.coffeeviz.dto;

import lombok.Data;

/**
 * 短信验证码登录请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class SmsLoginRequest {
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 验证码
     */
    private String code;
}
