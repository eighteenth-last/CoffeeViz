package com.coffeeviz.payment.service;

import com.coffeeviz.payment.enums.PaymentMethod;
import com.coffeeviz.payment.model.PaymentRequest;
import com.coffeeviz.payment.model.PaymentResponse;
import com.coffeeviz.payment.model.RefundRequest;
import com.coffeeviz.payment.model.RefundResponse;

import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse queryPayment(String orderNo);

    /**
     * 按支付方式查询订单状态
     */
    PaymentResponse queryPayment(String orderNo, PaymentMethod method);

    boolean handleCallback(PaymentMethod method, String callbackData);

    /**
     * 处理支付宝异步通知（form 参数）
     */
    boolean handleAlipayCallback(Map<String, String> params, Object orderService);

    /**
     * 处理微信支付 V3 回调（加密通知）
     */
    boolean handleWechatCallback(String timestamp, String nonce, String signature,
                                  String serial, String body, Object orderService);

    RefundResponse refund(RefundRequest request);

    boolean cancelOrder(String orderNo);
}
