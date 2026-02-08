package com.coffeeviz.payment.service.impl;

import com.coffeeviz.payment.enums.PaymentMethod;
import com.coffeeviz.payment.handler.AlipayPaymentHandler;
import com.coffeeviz.payment.handler.PaymentHandler;
import com.coffeeviz.payment.handler.StripePaymentHandler;
import com.coffeeviz.payment.handler.WechatPaymentHandler;
import com.coffeeviz.payment.model.*;
import com.coffeeviz.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final WechatPaymentHandler wechatHandler;
    private final AlipayPaymentHandler alipayHandler;
    private final StripePaymentHandler stripeHandler;
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("创建支付订单: method={}, orderNo={}", request.getMethod(), request.getOrderNo());
        
        PaymentHandler handler = getHandler(request.getMethod());
        return handler.createPayment(request);
    }
    
    @Override
    public PaymentResponse queryPayment(String orderNo) {
        log.info("查询支付订单: {}", orderNo);
        
        // TODO: 从数据库获取订单的支付方式
        // 这里暂时返回 null，实际应该查询数据库
        throw new UnsupportedOperationException("需要从数据库获取订单信息");
    }
    
    @Override
    public boolean handleCallback(PaymentMethod method, String callbackData) {
        log.info("处理支付回调: method={}", method);
        
        PaymentHandler handler = getHandler(method);
        return handler.handleCallback(callbackData);
    }
    
    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("处理退款: orderNo={}", request.getOrderNo());
        
        // TODO: 从数据库获取订单的支付方式
        throw new UnsupportedOperationException("需要从数据库获取订单信息");
    }
    
    @Override
    public boolean cancelOrder(String orderNo) {
        log.info("取消订单: {}", orderNo);
        
        // TODO: 从数据库获取订单的支付方式
        throw new UnsupportedOperationException("需要从数据库获取订单信息");
    }
    
    private PaymentHandler getHandler(PaymentMethod method) {
        switch (method) {
            case WECHAT:
                return wechatHandler;
            case ALIPAY:
                return alipayHandler;
            case STRIPE:
                return stripeHandler;
            default:
                throw new IllegalArgumentException("不支持的支付方式: " + method);
        }
    }
}
