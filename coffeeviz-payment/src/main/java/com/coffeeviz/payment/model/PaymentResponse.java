package com.coffeeviz.payment.model;

import com.coffeeviz.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private String orderNo;
    private String transactionId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String paymentUrl;
    private String qrCode;
    private LocalDateTime createTime;
    private LocalDateTime paymentTime;
    private String message;
}
