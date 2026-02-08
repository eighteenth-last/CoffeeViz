package com.coffeeviz.payment.service;

import com.coffeeviz.payment.enums.PaymentMethod;
import com.coffeeviz.payment.model.PaymentRequest;
import com.coffeeviz.payment.model.PaymentResponse;
import com.coffeeviz.payment.model.RefundRequest;
import com.coffeeviz.payment.model.RefundResponse;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 创建支付订单
     */
    PaymentResponse createPayment(PaymentRequest request);
    
    /**
     * 查询支付状态
     */
    PaymentResponse queryPayment(String orderNo);
    
    /**
     * 处理支付回调
     */
    boolean handleCallback(PaymentMethod method, String callbackData);
    
    /**
     * 退款
     */
    RefundResponse refund(RefundRequest request);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo);
}
