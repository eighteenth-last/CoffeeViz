package com.coffeeviz.exception;

/**
 * 支付验证异常
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
public class PaymentVerificationException extends RuntimeException {
    
    private String errorCode;
    
    public PaymentVerificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // 错误代码常量
    public static final String PAYMENT_NOT_FOUND = "PAYMENT_NOT_FOUND";
    public static final String PAYMENT_NOT_PAID = "PAYMENT_NOT_PAID";
    public static final String PAYMENT_AMOUNT_MISMATCH = "PAYMENT_AMOUNT_MISMATCH";
}
