package com.coffeeviz.payment.handler;

import com.coffeeviz.payment.config.PaymentConfig;
import com.coffeeviz.payment.enums.PaymentStatus;
import com.coffeeviz.payment.model.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 微信支付处理器 - Native 支付（扫码支付）
 * RSAAutoCertificateConfig 会自动下载微信平台证书，首次构建较慢（~20s），因此缓存实例复用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatPaymentHandler implements PaymentHandler {

    private final PaymentConfig paymentConfig;

    /** 缓存的微信支付服务实例，避免每次请求都重新下载平台证书 */
    private volatile NativePayService cachedService;
    /** 缓存的配置指纹，用于检测配置变更 */
    private volatile String cachedConfigFingerprint;

    /**
     * 获取或创建 NativePayService（带缓存）
     */
    private NativePayService getNativePayService() {
        PaymentConfig.WechatConfig cfg = paymentConfig.getWechat();
        String fingerprint = cfg.getMchId() + "|" + cfg.getApiV3Key() + "|" + cfg.getCertSerialNo();

        if (cachedService != null && fingerprint.equals(cachedConfigFingerprint)) {
            return cachedService;
        }

        synchronized (this) {
            // double-check
            if (cachedService != null && fingerprint.equals(cachedConfigFingerprint)) {
                return cachedService;
            }
            log.info("初始化微信支付 NativePayService（首次或配置变更）...");
            long start = System.currentTimeMillis();
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(cfg.getMchId())
                    .privateKey(cfg.getPrivateKey())
                    .merchantSerialNumber(cfg.getCertSerialNo())
                    .apiV3Key(cfg.getApiV3Key())
                    .build();
            cachedService = new NativePayService.Builder().config(config).build();
            cachedConfigFingerprint = fingerprint;
            log.info("微信支付 NativePayService 初始化完成，耗时 {}ms", System.currentTimeMillis() - start);
            return cachedService;
        }
    }

    /**
     * 清除缓存（配置变更时调用）
     */
    public void clearCache() {
        this.cachedService = null;
        this.cachedConfigFingerprint = null;
        log.info("微信支付客户端缓存已清除");
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("创建微信支付订单: {}", request.getOrderNo());
        PaymentConfig.WechatConfig cfg = paymentConfig.getWechat();

        if (!cfg.isEnabled() || cfg.getMchId() == null || cfg.getMchId().isEmpty()) {
            log.warn("微信支付未启用或未配置，返回模拟支付");
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo())
                    .status(PaymentStatus.PENDING)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .qrCode("WECHAT_NOT_CONFIGURED")
                    .message("微信支付未配置，请在管理端配置支付参数")
                    .build();
        }

        try {
            NativePayService service = getNativePayService();

            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(cfg.getAppId());
            prepayRequest.setMchid(cfg.getMchId());
            prepayRequest.setDescription(request.getSubject() != null ? request.getSubject() : "CoffeeViz 订阅");
            prepayRequest.setOutTradeNo(request.getOrderNo());
            // 优先使用 request 中传入的 notifyUrl（已由 controller 构建为完整 URL + 正确路径）
            String notifyUrl = (request.getNotifyUrl() != null && request.getNotifyUrl().startsWith("http"))
                    ? request.getNotifyUrl()
                    : cfg.getNotifyUrl();
            if (notifyUrl != null && !notifyUrl.isEmpty()) {
                prepayRequest.setNotifyUrl(notifyUrl);
                log.info("微信支付回调地址: {}", notifyUrl);
            }

            Amount amount = new Amount();
            // 微信支付金额单位为分
            amount.setTotal(request.getAmount().multiply(new BigDecimal("100")).intValue());
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            PrepayResponse response = service.prepay(prepayRequest);

            log.info("微信支付预下单成功: orderNo={}, codeUrl={}", request.getOrderNo(), response.getCodeUrl());
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo())
                    .status(PaymentStatus.PENDING)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .qrCode(response.getCodeUrl())
                    .message("微信支付订单创建成功")
                    .build();

        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            // 如果是证书/配置问题，清除缓存以便下次重试
            clearCache();
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo())
                    .status(PaymentStatus.FAILED)
                    .message("创建微信支付订单失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse queryPayment(String orderNo) {
        log.info("查询微信支付订单: {}", orderNo);
        try {
            NativePayService service = getNativePayService();
            PaymentConfig.WechatConfig cfg = paymentConfig.getWechat();

            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(cfg.getMchId());
            request.setOutTradeNo(orderNo);

            Transaction transaction = service.queryOrderByOutTradeNo(request);
            PaymentStatus status = mapWechatStatus(transaction.getTradeState());

            return PaymentResponse.builder()
                    .orderNo(orderNo)
                    .transactionId(transaction.getTransactionId())
                    .status(status)
                    .build();
        } catch (Exception e) {
            log.error("查询微信支付订单失败", e);
            throw new RuntimeException("查询支付订单失败", e);
        }
    }

    @Override
    public boolean handleCallback(String callbackData) {
        log.info("处理微信支付回调");
        return true;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("微信支付退款: {}", request.getOrderNo());
        return RefundResponse.builder()
                .orderNo(request.getOrderNo())
                .refundAmount(request.getRefundAmount())
                .status("PENDING")
                .message("退款功能待实现")
                .build();
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        log.info("取消微信支付订单: {}", orderNo);
        try {
            NativePayService service = getNativePayService();
            PaymentConfig.WechatConfig cfg = paymentConfig.getWechat();

            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(cfg.getMchId());
            request.setOutTradeNo(orderNo);

            service.closeOrder(request);
            return true;
        } catch (Exception e) {
            log.error("取消微信支付订单失败", e);
            return false;
        }
    }

    private PaymentStatus mapWechatStatus(Transaction.TradeStateEnum tradeState) {
        if (tradeState == null) return PaymentStatus.PENDING;
        return switch (tradeState) {
            case SUCCESS -> PaymentStatus.PAID;
            case CLOSED -> PaymentStatus.CANCELLED;
            case REVOKED -> PaymentStatus.CANCELLED;
            case REFUND -> PaymentStatus.REFUNDED;
            case NOTPAY, USERPAYING -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
}
