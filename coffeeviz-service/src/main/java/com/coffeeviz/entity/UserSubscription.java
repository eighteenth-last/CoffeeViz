package com.coffeeviz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户订阅实体
 */
@Data
@TableName("biz_user_subscription")
public class UserSubscription {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long planId;
    
    private String planCode;
    
    private String billingCycle;
    
    private BigDecimal price;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer autoRenew;
    
    private String status;
    
    private String paymentMethod;
    
    private String transactionId;
    
    private LocalDateTime cancelTime;
    
    private String cancelReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
