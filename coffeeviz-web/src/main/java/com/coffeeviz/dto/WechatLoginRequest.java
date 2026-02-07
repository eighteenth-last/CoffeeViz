package com.coffeeviz.dto;

import lombok.Data;

/**
 * 微信登录请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class WechatLoginRequest {
    
    /**
     * 微信授权码
     */
    private String code;
    
    /**
     * 微信 openId
     */
    private String openId;
    
    /**
     * 微信昵称
     */
    private String nickname;
    
    /**
     * 微信头像
     */
    private String avatarUrl;
}
