package com.coffeeviz.payment.enums;

/**
 * 支付方式枚举
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
public enum PaymentMethod {
    
    /**
     * 微信支付
     */
    WECHAT("wechat", "微信支付"),
    
    /**
     * 支付宝
     */
    ALIPAY("alipay", "支付宝"),
    
    /**
     * Stripe (国际支付)
     */
    STRIPE("stripe", "Stripe"),
    
    /**
     * 余额支付
     */
    BALANCE("balance", "余额支付");
    
    private final String code;
    private final String name;
    
    PaymentMethod(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + code);
    }
}
