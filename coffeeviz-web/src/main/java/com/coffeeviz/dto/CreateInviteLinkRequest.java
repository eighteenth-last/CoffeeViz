package com.coffeeviz.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建邀请链接请求
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Data
public class CreateInviteLinkRequest {
    
    /**
     * 最大使用次数，0或null表示无限制
     */
    private Integer maxUses;
    
    /**
     * 过期时间，null表示永久有效
     */
    private LocalDateTime expireTime;
}
