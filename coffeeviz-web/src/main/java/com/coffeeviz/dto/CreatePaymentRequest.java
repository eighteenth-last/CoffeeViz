package com.coffeeviz.dto;

import lombok.Data;

/**
 * 创建支付请求
 */
@Data
public class CreatePaymentRequest {
    
    private Long planId;
    private String billingCycle;
    private String paymentMethod;
}
