package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 */
@Data
@TableName("biz_payment_order")
public class PaymentOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Long subscriptionId;
    
    private Long planId;
    
    private String planCode;
    
    private String billingCycle;
    
    private BigDecimal amount;
    
    private String currency;
    
    private String paymentMethod;
    
    private String paymentStatus;
    
    private String transactionId;
    
    private LocalDateTime paymentTime;
    
    private LocalDateTime refundTime;
    
    private String refundReason;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
