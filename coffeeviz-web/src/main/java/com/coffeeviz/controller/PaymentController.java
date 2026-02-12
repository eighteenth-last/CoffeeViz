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
    private final com.coffeeviz.mapper.SysConfigGroupMapper configGroupMapper;

    /**
     * 获取可用的支付方式（前端展示用，从独立记录读取 status=1 的支付方式）
     */
    @GetMapping("/methods")
    public Result<List<java.util.Map<String, Object>>> getAvailablePaymentMethods() {
        List<java.util.Map<String, Object>> methods = new java.util.ArrayList<>();
        try {
            List<com.coffeeviz.entity.SysConfigGroup> groups = configGroupMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.coffeeviz.entity.SysConfigGroup>()
                            .likeRight(com.coffeeviz.entity.SysConfigGroup::getGroupCode, "payment_")
                            .eq(com.coffeeviz.entity.SysConfigGroup::getStatus, 1)
                            .orderByAsc(com.coffeeviz.entity.SysConfigGroup::getSort));

            for (com.coffeeviz.entity.SysConfigGroup group : groups) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                switch (group.getGroupCode()) {
                    case "payment_alipay" -> {
                        m.put("code", "ALIPAY");
                        m.put("name", "支付宝");
                        m.put("icon", "fab fa-alipay");
                        m.put("color", "blue");
                    }
                    case "payment_wechat" -> {
                        m.put("code", "WECHAT");
                        m.put("name", "微信支付");
                        m.put("icon", "fab fa-weixin");
                        m.put("color", "green");
                    }
                    case "payment_test" -> {
                        m.put("code", "TEST");
                        m.put("name", "测试支付");
                        m.put("icon", "fas fa-flask");
                        m.put("color", "amber");
                    }
                    default -> { continue; }
                }
                methods.add(m);
            }
        } catch (Exception e) {
            log.error("获取支付方式失败", e);
        }

        // 如果没有配置任何支付方式，返回默认的测试支付
        if (methods.isEmpty()) {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("code", "TEST");
            m.put("name", "测试支付");
            m.put("icon", "fas fa-flask");
            m.put("color", "amber");
            methods.add(m);
        }

        return Result.success(methods);
    }
    
    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request,
                                                  jakarta.servlet.http.HttpServletRequest httpRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 创建订单
        PaymentOrder order = orderService.createOrder(
            userId,
            request.getPlanId(),
            request.getBillingCycle(),
            request.getPaymentMethod()
        );
        
        // 构建回调 URL：优先使用数据库配置的 notifyUrl 的域名部分 + 正确的回调路径
        String methodLower = request.getPaymentMethod().toLowerCase();
        String callbackPath = "/api/payment/callback/" + methodLower;
        String notifyUrl = buildNotifyUrl(methodLower, callbackPath);
        
        // 调用支付接口
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderNo(order.getOrderNo())
            .amount(order.getAmount())
            .currency(order.getCurrency())
            .method(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
            .subject("CoffeeViz 订阅 - " + order.getPlanCode())
            .userId(userId.toString())
            .notifyUrl(notifyUrl)
            .build();
        
        log.info("创建支付请求: orderNo={}, notifyUrl={}", order.getOrderNo(), notifyUrl);
        PaymentResponse response = paymentService.createPayment(paymentRequest);
        return Result.success(response);
    }
    
    /**
     * 从数据库支付配置中提取域名，拼接正确的回调路径
     */
    private String buildNotifyUrl(String method, String callbackPath) {
        try {
            String groupCode = "alipay".equals(method) ? "payment_alipay" : "payment_wechat";
            com.coffeeviz.entity.SysConfigGroup group = configGroupMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.coffeeviz.entity.SysConfigGroup>()
                            .eq(com.coffeeviz.entity.SysConfigGroup::getGroupCode, groupCode));
            if (group != null && group.getConfigValue() != null) {
                com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(group.getConfigValue());
                String configuredUrl = json.getString("notifyUrl");
                if (configuredUrl != null && configuredUrl.startsWith("http")) {
                    try {
                        java.net.URI uri = new java.net.URI(configuredUrl);
                        String baseUrl = uri.getScheme() + "://" + uri.getHost();
                        if (uri.getPort() > 0 && uri.getPort() != 80 && uri.getPort() != 443) {
                            baseUrl += ":" + uri.getPort();
                        }
                        return baseUrl + callbackPath;
                    } catch (Exception e) {
                        log.warn("解析 notifyUrl 失败: {}", configuredUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("构建 notifyUrl 失败", e);
        }
        return callbackPath;
    }
    
    /**
     * 确认支付（用户点击"我已完成支付"后，主动查询支付平台确认状态）
     */
    @PostMapping("/confirm/{orderNo}")
    public Result<String> confirmPayment(@PathVariable("orderNo") String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        PaymentOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        if ("paid".equals(order.getPaymentStatus())) {
            return Result.success("订单已支付", "success");
        }

        // TEST 支付方式直接模拟成功
        if ("TEST".equalsIgnoreCase(order.getPaymentMethod())) {
            String transactionId = "TEST_" + System.currentTimeMillis();
            boolean success = orderService.handlePaymentSuccess(orderNo, transactionId);
            return success ? Result.success("支付成功，订阅已生效", "success") : Result.error("支付确认失败");
        }

        // 真实支付：主动查询支付平台
        try {
            PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod().toUpperCase());
            PaymentResponse queryResult = paymentService.queryPayment(orderNo, method);

            if (queryResult != null && queryResult.getStatus() == com.coffeeviz.payment.enums.PaymentStatus.PAID) {
                boolean success = orderService.handlePaymentSuccess(orderNo, queryResult.getTransactionId());
                return success ? Result.success("支付成功，订阅已生效", "success") : Result.error("支付确认失败");
            } else {
                String msg = (queryResult != null && queryResult.getMessage() != null)
                        ? queryResult.getMessage()
                        : "支付尚未完成，请完成支付后重试";
                return Result.error(402, msg);
            }
        } catch (Exception e) {
            log.error("查询支付状态失败: orderNo={}", orderNo, e);
            return Result.error(402, "查询支付状态失败，请稍后重试");
        }
    }
    
    /**
     * 前端轮询支付状态（先查数据库，如果还是 pending 则主动查询支付平台）
     */
    @GetMapping("/status/{orderNo}")
    public Result<java.util.Map<String, Object>> getPaymentStatus(@PathVariable("orderNo") String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        PaymentOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }

        // 如果数据库已经是 paid，直接返回
        if ("paid".equals(order.getPaymentStatus())) {
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("status", "paid");
            result.put("paid", true);
            return Result.success(result);
        }

        // 如果订单已取消，直接返回
        if ("cancelled".equals(order.getPaymentStatus())) {
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("orderNo", order.getOrderNo());
            result.put("status", "cancelled");
            result.put("paid", false);
            return Result.success(result);
        }

        // 数据库还是 pending，主动查询支付平台（TEST 支付除外）
        if (!"TEST".equalsIgnoreCase(order.getPaymentMethod())) {
            try {
                PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod().toUpperCase());
                PaymentResponse queryResult = paymentService.queryPayment(orderNo, method);
                if (queryResult != null && queryResult.getStatus() == com.coffeeviz.payment.enums.PaymentStatus.PAID) {
                    // 支付平台确认已支付，更新本地订单
                    orderService.handlePaymentSuccess(orderNo, queryResult.getTransactionId());
                    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("orderNo", order.getOrderNo());
                    result.put("status", "paid");
                    result.put("paid", true);
                    return Result.success(result);
                }
            } catch (Exception e) {
                log.warn("轮询时查询支付平台失败: orderNo={}, error={}", orderNo, e.getMessage());
                // 查询失败不影响返回，继续返回数据库状态
            }
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getPaymentStatus());
        result.put("paid", false);
        return Result.success(result);
    }
    
    /**
     * 取消支付订单（用户关闭支付弹窗时调用）
     */
    @PostMapping("/cancel/{orderNo}")
    public Result<String> cancelPayment(@PathVariable("orderNo") String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();

        PaymentOrder order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        // 已支付的订单不能取消
        if ("paid".equals(order.getPaymentStatus())) {
            return Result.success("订单已支付", "paid");
        }
        // 已取消的订单直接返回
        if ("cancelled".equals(order.getPaymentStatus())) {
            return Result.success("订单已取消", "cancelled");
        }

        // 更新本地订单状态
        orderService.updateOrderStatus(orderNo, "cancelled", null);
        log.info("订单已取消: orderNo={}", orderNo);

        // 异步关闭支付平台订单（不阻塞用户）
        try {
            if (!"TEST".equalsIgnoreCase(order.getPaymentMethod())) {
                PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod().toUpperCase());
                paymentService.cancelOrder(orderNo);
                log.info("支付平台订单已关闭: orderNo={}, method={}", orderNo, method);
            }
        } catch (Exception e) {
            // 关闭支付平台订单失败不影响本地取消
            log.warn("关闭支付平台订单失败（不影响本地取消）: orderNo={}, error={}", orderNo, e.getMessage());
        }

        return Result.success("订单已取消", "cancelled");
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/query/{orderNo}")
    public Result<PaymentOrder> queryPayment(@PathVariable("orderNo") String orderNo) {
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
     * 微信支付回调（V3 加密通知）
     */
    @PostMapping("/callback/wechat")
    public String wechatCallback(
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestBody String body) {
        log.info("收到微信支付回调");
        try {
            boolean success = paymentService.handleWechatCallback(timestamp, nonce, signature, serial, body, orderService);
            return success ? "{\"code\":\"SUCCESS\",\"message\":\"成功\"}" : "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 支付宝异步通知（form 表单参数）
     */
    @PostMapping("/callback/alipay")
    public String alipayCallback(jakarta.servlet.http.HttpServletRequest request) {
        log.info("收到支付宝异步通知");
        try {
            java.util.Map<String, String> params = new java.util.HashMap<>();
            java.util.Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                StringBuilder valueStr = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
                }
                params.put(name, valueStr.toString());
            }

            boolean success = paymentService.handleAlipayCallback(params, orderService);
            return success ? "success" : "fail";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "fail";
        }
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
