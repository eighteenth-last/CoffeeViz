package com.coffeeviz.dto;

import lombok.Data;

/**
 * 赠送订阅请求
 */
@Data
public class GiftSubscriptionRequest {
    private Long userId;
    private String plan;
    private String reason;
}
