package com.coffeeviz.payment.handler;

import com.coffeeviz.payment.model.*;

/**
 * 支付处理器接口
 */
public interface PaymentHandler {
    
    /**
     * 创建支付
     */
    PaymentResponse createPayment(PaymentRequest request);
    
    /**
     * 查询支付
     */
    PaymentResponse queryPayment(String orderNo);
    
    /**
     * 处理回调
     */
    boolean handleCallback(String callbackData);
    
    /**
     * 退款
     */
    RefundResponse refund(RefundRequest request);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo);
}
