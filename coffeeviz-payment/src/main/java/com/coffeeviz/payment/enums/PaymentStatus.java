package com.coffeeviz.payment.enums;

/**
 * 支付状态枚举
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
public enum PaymentStatus {
    
    /**
     * 待支付
     */
    PENDING("pending", "待支付"),
    
    /**
     * 支付中
     */
    PROCESSING("processing", "支付中"),
    
    /**
     * 支付成功
     */
    PAID("paid", "支付成功"),
    
    /**
     * 支付失败
     */
    FAILED("failed", "支付失败"),
    
    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),
    
    /**
     * 已退款
     */
    REFUNDED("refunded", "已退款"),
    
    /**
     * 部分退款
     */
    PARTIAL_REFUNDED("partial_refunded", "部分退款");
    
    private final String code;
    private final String name;
    
    PaymentStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + code);
    }
}
