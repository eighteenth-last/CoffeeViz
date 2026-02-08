package com.coffeeviz.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    
    private String orderNo;
    private String transactionId;
    private BigDecimal refundAmount;
    private String reason;
    private String requestId;
}
