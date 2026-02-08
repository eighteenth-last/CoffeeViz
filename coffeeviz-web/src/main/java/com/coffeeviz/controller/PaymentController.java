package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.dto.CreatePaymentRequest;
import com.coffeeviz.entity.PaymentOrder;
import com.coffeeviz.payment.enums.PaymentMethod;
import com.coffeeviz.payment.model.PaymentRequest;
import com.coffeeviz.payment.model.PaymentResponse;
import com.coffeeviz.payment.service.PaymentService;
import com.coffeeviz.service.PaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentOrderService orderService;
    private final PaymentService paymentService;
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 创建订单
        PaymentOrder order = orderService.createOrder(
            userId,
            request.getPlanId(),
            request.getBillingCycle(),
            request.getPaymentMethod()
        );
        
        // 调用支付接口
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderNo(order.getOrderNo())
            .amount(order.getAmount())
            .currency(order.getCurrency())
            .method(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
            .subject("CoffeeViz 订阅 - " + order.getPlanCode())
            .userId(userId.toString())
            .notifyUrl("/api/payment/callback/" + request.getPaymentMethod())
            .build();
        
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        return Result.success(response);
    }
    
    /**
     * 查询支付状态
     */
    @GetMapping("/query/{orderNo}")
    public Result<PaymentOrder> queryPayment(@PathVariable String orderNo) {
        PaymentOrder order = orderService.getByOrderNo(orderNo);
        return Result.success(order);
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders")
    public Result<List<PaymentOrder>> getUserOrders() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PaymentOrder> orders = orderService.getUserOrders(userId);
        return Result.success(orders);
    }
    
    /**
     * 微信支付回调
     */
    @PostMapping("/callback/wechat")
    public String wechatCallback(@RequestBody String callbackData) {
        log.info("收到微信支付回调");
        boolean success = paymentService.handleCallback(PaymentMethod.WECHAT, callbackData);
        return success ? "SUCCESS" : "FAIL";
    }
    
    /**
     * 支付宝回调
     */
    @PostMapping("/callback/alipay")
    public String alipayCallback(@RequestBody String callbackData) {
        log.info("收到支付宝回调");
        boolean success = paymentService.handleCallback(PaymentMethod.ALIPAY, callbackData);
        return success ? "success" : "fail";
    }
    
    /**
     * Stripe Webhook
     */
    @PostMapping("/callback/stripe")
    public String stripeWebhook(@RequestBody String callbackData) {
        log.info("收到 Stripe Webhook");
        boolean success = paymentService.handleCallback(PaymentMethod.STRIPE, callbackData);
        return success ? "success" : "fail";
    }
}
