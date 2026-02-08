package com.coffeeviz.service;

import com.coffeeviz.entity.PaymentOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付订单服务接口
 */
public interface PaymentOrderService {
    
    /**
     * 创建支付订单
     */
    PaymentOrder createOrder(Long userId, Long planId, String billingCycle, String paymentMethod);
    
    /**
     * 根据订单号查询
     */
    PaymentOrder getByOrderNo(String orderNo);
    
    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderNo, String status, String transactionId);
    
    /**
     * 支付成功回调
     */
    boolean handlePaymentSuccess(String orderNo, String transactionId);
    
    /**
     * 退款
     */
    boolean refundOrder(String orderNo, String reason);
    
    /**
     * 获取用户订单列表
     */
    List<PaymentOrder> getUserOrders(Long userId);
}
