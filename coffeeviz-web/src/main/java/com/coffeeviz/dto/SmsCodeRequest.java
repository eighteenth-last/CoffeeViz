package com.coffeeviz.dto;

import lombok.Data;

/**
 * 发送短信验证码请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class SmsCodeRequest {
    
    /**
     * 手机号
     */
    private String phone;
}
