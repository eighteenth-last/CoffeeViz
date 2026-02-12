package com.coffeeviz.payment.service.impl;

import com.coffeeviz.payment.config.PaymentConfig;
import com.coffeeviz.payment.enums.PaymentMethod;
import com.coffeeviz.payment.enums.PaymentStatus;
import com.coffeeviz.payment.handler.AlipayPaymentHandler;
import com.coffeeviz.payment.handler.PaymentHandler;
import com.coffeeviz.payment.handler.StripePaymentHandler;
import com.coffeeviz.payment.handler.WechatPaymentHandler;
import com.coffeeviz.payment.model.*;
import com.coffeeviz.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

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
    private final PaymentConfig paymentConfig;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("创建支付订单: method={}, orderNo={}", request.getMethod(), request.getOrderNo());
        PaymentHandler handler = getHandler(request.getMethod());
        return handler.createPayment(request);
    }

    @Override
    public PaymentResponse queryPayment(String orderNo) {
        throw new UnsupportedOperationException("需要指定支付方式，请使用 queryPayment(orderNo, method)");
    }

    @Override
    public PaymentResponse queryPayment(String orderNo, PaymentMethod method) {
        log.info("查询支付订单: orderNo={}, method={}", orderNo, method);
        PaymentHandler handler = getHandler(method);
        return handler.queryPayment(orderNo);
    }

    @Override
    public boolean handleCallback(PaymentMethod method, String callbackData) {
        log.info("处理支付回调: method={}", method);
        PaymentHandler handler = getHandler(method);
        return handler.handleCallback(callbackData);
    }

    @Override
    public boolean handleAlipayCallback(Map<String, String> params, Object orderService) {
        log.info("处理支付宝异步通知: out_trade_no={}", params.get("out_trade_no"));
        try {
            // 1. 验签
            PaymentConfig.AlipayConfig cfg = paymentConfig.getAlipay();
            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params, cfg.getPublicKey(), cfg.getCharset(), cfg.getSignType());

            if (!signVerified) {
                log.error("支付宝回调验签失败");
                return false;
            }

            // 2. 检查交易状态
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            log.info("支付宝回调验签成功: orderNo={}, tradeNo={}, status={}", outTradeNo, tradeNo, tradeStatus);

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 3. 调用 orderService.handlePaymentSuccess 通过反射（避免循环依赖）
                return invokeHandlePaymentSuccess(orderService, outTradeNo, tradeNo);
            }

            return true;
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return false;
        }
    }

    @Override
    public boolean handleWechatCallback(String timestamp, String nonce, String signature,
                                         String serial, String body, Object orderService) {
        log.info("处理微信支付回调通知");
        try {
            PaymentConfig.WechatConfig cfg = paymentConfig.getWechat();

            // 使用 SDK 解密通知
            com.wechat.pay.java.core.notification.NotificationConfig notificationConfig =
                    new com.wechat.pay.java.core.RSAAutoCertificateConfig.Builder()
                            .merchantId(cfg.getMchId())
                            .privateKey(cfg.getPrivateKey())
                            .merchantSerialNumber(cfg.getCertSerialNo())
                            .apiV3Key(cfg.getApiV3Key())
                            .build();

            com.wechat.pay.java.core.notification.NotificationParser parser =
                    new com.wechat.pay.java.core.notification.NotificationParser(notificationConfig);

            com.wechat.pay.java.core.notification.RequestParam requestParam =
                    new com.wechat.pay.java.core.notification.RequestParam.Builder()
                            .serialNumber(serial)
                            .nonce(nonce)
                            .timestamp(timestamp)
                            .signature(signature)
                            .body(body)
                            .build();

            com.wechat.pay.java.service.payments.model.Transaction transaction =
                    parser.parse(requestParam, com.wechat.pay.java.service.payments.model.Transaction.class);

            String outTradeNo = transaction.getOutTradeNo();
            String transactionId = transaction.getTransactionId();
            com.wechat.pay.java.service.payments.model.Transaction.TradeStateEnum tradeState = transaction.getTradeState();

            log.info("微信回调解密成功: orderNo={}, transactionId={}, state={}", outTradeNo, transactionId, tradeState);

            if (tradeState == com.wechat.pay.java.service.payments.model.Transaction.TradeStateEnum.SUCCESS) {
                return invokeHandlePaymentSuccess(orderService, outTradeNo, transactionId);
            }

            return true;
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return false;
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("处理退款: orderNo={}", request.getOrderNo());
        throw new UnsupportedOperationException("需要从数据库获取订单信息");
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        log.info("取消订单: {}", orderNo);
        throw new UnsupportedOperationException("需要从数据库获取订单信息");
    }

    private PaymentHandler getHandler(PaymentMethod method) {
        return switch (method) {
            case WECHAT -> wechatHandler;
            case ALIPAY -> alipayHandler;
            case STRIPE -> stripeHandler;
            case TEST -> new TestPaymentHandler();
            default -> throw new IllegalArgumentException("不支持的支付方式: " + method);
        };
    }

    /**
     * 通过反射调用 orderService.handlePaymentSuccess，避免 payment 模块对 service 模块的循环依赖
     */
    private boolean invokeHandlePaymentSuccess(Object orderService, String orderNo, String transactionId) {
        try {
            Method method = orderService.getClass().getMethod("handlePaymentSuccess", String.class, String.class);
            Object result = method.invoke(orderService, orderNo, transactionId);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("调用 handlePaymentSuccess 失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    /**
     * 测试支付处理器
     */
    private static class TestPaymentHandler implements PaymentHandler {
        @Override
        public PaymentResponse createPayment(PaymentRequest request) {
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo()).status(PaymentStatus.PENDING)
                    .amount(request.getAmount()).currency(request.getCurrency())
                    .qrCode("TEST_PAYMENT_" + request.getOrderNo())
                    .message("测试支付订单已创建").build();
        }

        @Override
        public PaymentResponse queryPayment(String orderNo) {
            return PaymentResponse.builder().orderNo(orderNo).status(PaymentStatus.PAID).build();
        }

        @Override
        public boolean handleCallback(String callbackData) { return true; }

        @Override
        public RefundResponse refund(RefundRequest request) {
            return RefundResponse.builder().orderNo(request.getOrderNo())
                    .refundAmount(request.getRefundAmount()).status("SUCCESS").message("测试退款成功").build();
        }

        @Override
        public boolean cancelOrder(String orderNo) { return true; }
    }
}
