package com.coffeeviz.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private String orderNo;
    private String refundId;
    private BigDecimal refundAmount;
    private String status;
    private LocalDateTime refundTime;
    private String message;
}
