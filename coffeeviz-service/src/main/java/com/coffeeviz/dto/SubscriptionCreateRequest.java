package com.coffeeviz.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 订阅创建请求 DTO
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
public class SubscriptionCreateRequest {
    
    @NotNull(message = "用户ID必填")
    private Long userId;
    
    @NotNull(message = "计划ID必填")
    private Long planId;
    
    @NotNull(message = "支付订单ID必填")
    private Long paymentOrderId;
    
    @NotNull(message = "计费周期必填")
    private String billingCycle;  // "monthly" 或 "yearly"
}
