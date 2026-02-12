package com.coffeeviz.payment.handler;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.coffeeviz.payment.config.PaymentConfig;
import com.coffeeviz.payment.enums.PaymentStatus;
import com.coffeeviz.payment.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付处理器 - 当面付（扫码支付 precreate）
 * AlipayClient 实例缓存复用，避免每次请求都重新创建。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentHandler implements PaymentHandler {

    private final PaymentConfig paymentConfig;

    /** 缓存的支付宝客户端实例 */
    private volatile AlipayClient cachedClient;
    /** 缓存的配置指纹 */
    private volatile String cachedConfigFingerprint;

    private AlipayClient getClient() throws AlipayApiException {
        PaymentConfig.AlipayConfig cfg = paymentConfig.getAlipay();
        String fingerprint = cfg.getAppId() + "|" + cfg.getGatewayUrl() + "|" + cfg.getSignType();

        if (cachedClient != null && fingerprint.equals(cachedConfigFingerprint)) {
            return cachedClient;
        }

        synchronized (this) {
            if (cachedClient != null && fingerprint.equals(cachedConfigFingerprint)) {
                return cachedClient;
            }
            log.info("初始化支付宝 AlipayClient（首次或配置变更）...");
            AlipayConfig alipayConfig = new AlipayConfig();
            alipayConfig.setServerUrl(cfg.getGatewayUrl());
            alipayConfig.setAppId(cfg.getAppId());
            alipayConfig.setPrivateKey(cfg.getPrivateKey());
            alipayConfig.setAlipayPublicKey(cfg.getPublicKey());
            alipayConfig.setCharset(cfg.getCharset());
            alipayConfig.setSignType(cfg.getSignType());
            alipayConfig.setFormat("json");
            cachedClient = new DefaultAlipayClient(alipayConfig);
            cachedConfigFingerprint = fingerprint;
            log.info("支付宝 AlipayClient 初始化完成");
            return cachedClient;
        }
    }

    /**
     * 清除缓存（配置变更时调用）
     */
    public void clearCache() {
        this.cachedClient = null;
        this.cachedConfigFingerprint = null;
        log.info("支付宝客户端缓存已清除");
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("创建支付宝订单: {}", request.getOrderNo());
        PaymentConfig.AlipayConfig cfg = paymentConfig.getAlipay();

        if (!cfg.isEnabled() || cfg.getAppId() == null || cfg.getAppId().isEmpty()) {
            log.warn("支付宝未启用或未配置，返回模拟支付");
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo())
                    .status(PaymentStatus.PENDING)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .paymentUrl("ALIPAY_NOT_CONFIGURED")
                    .message("支付宝未配置，请在管理端配置支付参数")
                    .build();
        }

        try {
            AlipayClient client = getClient();

            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(request.getOrderNo());
            model.setTotalAmount(request.getAmount().toPlainString());
            model.setSubject(request.getSubject() != null ? request.getSubject() : "CoffeeViz 订阅");

            AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
            alipayRequest.setBizModel(model);
            // 优先使用 request 中传入的 notifyUrl（已由 controller 构建为完整 URL + 正确路径）
            String notifyUrl = (request.getNotifyUrl() != null && request.getNotifyUrl().startsWith("http"))
                    ? request.getNotifyUrl()
                    : cfg.getNotifyUrl();
            if (notifyUrl != null && !notifyUrl.isEmpty()) {
                alipayRequest.setNotifyUrl(notifyUrl);
                log.info("支付宝回调地址: {}", notifyUrl);
            }

            AlipayTradePrecreateResponse response = client.execute(alipayRequest);

            if (response.isSuccess()) {
                log.info("支付宝预下单成功: orderNo={}, qrCode={}", request.getOrderNo(), response.getQrCode());
                return PaymentResponse.builder()
                        .orderNo(request.getOrderNo())
                        .status(PaymentStatus.PENDING)
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .qrCode(response.getQrCode())
                        .message("支付宝订单创建成功")
                        .build();
            } else {
                log.error("支付宝预下单失败: code={}, msg={}, subCode={}, subMsg={}",
                        response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());
                return PaymentResponse.builder()
                        .orderNo(request.getOrderNo())
                        .status(PaymentStatus.FAILED)
                        .message("支付宝下单失败: " + response.getSubMsg())
                        .build();
            }
        } catch (Exception e) {
            log.error("创建支付宝订单异常", e);
            clearCache();
            return PaymentResponse.builder()
                    .orderNo(request.getOrderNo())
                    .status(PaymentStatus.FAILED)
                    .message("创建支付订单失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentResponse queryPayment(String orderNo) {
        log.info("查询支付宝订单: {}", orderNo);
        try {
            AlipayClient client = getClient();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderNo);

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizModel(model);

            AlipayTradeQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                PaymentStatus status = mapAlipayStatus(response.getTradeStatus());
                return PaymentResponse.builder()
                        .orderNo(orderNo)
                        .transactionId(response.getTradeNo())
                        .status(status)
                        .build();
            }
            // 交易不存在等业务错误，返回具体状态而非 PENDING
            String subCode = response.getSubCode();
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                log.info("支付宝交易不存在: orderNo={}", orderNo);
                return PaymentResponse.builder().orderNo(orderNo).status(PaymentStatus.PENDING)
                        .message("交易不存在，可能尚未扫码支付").build();
            }
            log.warn("查询支付宝订单业务失败: orderNo={}, subCode={}, subMsg={}", orderNo, subCode, response.getSubMsg());
            return PaymentResponse.builder().orderNo(orderNo).status(PaymentStatus.PENDING)
                    .message(response.getSubMsg()).build();
        } catch (Exception e) {
            log.error("查询支付宝订单失败", e);
            throw new RuntimeException("查询支付订单失败", e);
        }
    }

    @Override
    public boolean handleCallback(String callbackData) {
        log.info("处理支付宝回调");
        return true;
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("支付宝退款: {}", request.getOrderNo());
        try {
            AlipayClient client = getClient();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(request.getOrderNo());
            model.setRefundAmount(request.getRefundAmount().toPlainString());
            model.setRefundReason(request.getReason());

            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            alipayRequest.setBizModel(model);

            AlipayTradeRefundResponse response = client.execute(alipayRequest);
            return RefundResponse.builder()
                    .orderNo(request.getOrderNo())
                    .refundAmount(request.getRefundAmount())
                    .status(response.isSuccess() ? "SUCCESS" : "FAILED")
                    .message(response.isSuccess() ? "退款成功" : response.getSubMsg())
                    .build();
        } catch (Exception e) {
            log.error("支付宝退款失败", e);
            throw new RuntimeException("退款失败", e);
        }
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        log.info("取消支付宝订单: {}", orderNo);
        try {
            AlipayClient client = getClient();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(orderNo);

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            request.setBizModel(model);

            AlipayTradeCloseResponse response = client.execute(request);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("取消支付宝订单失败", e);
            return false;
        }
    }

    private PaymentStatus mapAlipayStatus(String tradeStatus) {
        if (tradeStatus == null) return PaymentStatus.PENDING;
        return switch (tradeStatus) {
            case "TRADE_SUCCESS", "TRADE_FINISHED" -> PaymentStatus.PAID;
            case "TRADE_CLOSED" -> PaymentStatus.CANCELLED;
            case "WAIT_BUYER_PAY" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
}
