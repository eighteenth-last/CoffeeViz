package com.coffeeviz.payment.model;

import com.coffeeviz.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付请求
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 订单标题
     */
    private String subject;
    
    /**
     * 订单描述
     */
    private String description;
    
    /**
     * 支付金额（元）
     */
    private BigDecimal amount;
    
    /**
     * 货币类型（CNY/USD）
     */
    private String currency;
    
    /**
     * 支付方式
     */
    private PaymentMethod method;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户IP
     */
    private String userIp;
    
    /**
     * 回调通知URL
     */
    private String notifyUrl;
    
    /**
     * 前端回调URL
     */
    private String returnUrl;
    
    /**
     * 附加数据
     */
    private String attach;
}
