package com.coffeeviz.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置（支持 application.yml 静态配置 + 数据库动态配置）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {

    private WechatConfig wechat = new WechatConfig();
    private AlipayConfig alipay = new AlipayConfig();
    private StripeConfig stripe = new StripeConfig();
    private TestConfig test = new TestConfig();

    /** 是否优先使用数据库配置 */
    private boolean preferDatabase = true;

    @Data
    public static class WechatConfig {
        private boolean enabled;
        private String appId;
        private String mchId;
        private String apiKey;
        private String apiV3Key;
        private String privateKey;
        private String certSerialNo;
        private String certPath;
        private String notifyUrl;
    }

    @Data
    public static class AlipayConfig {
        private boolean enabled;
        private String appId;
        private String privateKey;
        private String publicKey;
        private String signType = "RSA2";
        private String charset = "UTF-8";
        private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
        private String notifyUrl;
        private String returnUrl;
    }

    @Data
    public static class StripeConfig {
        private String apiKey;
        private String webhookSecret;
        private String successUrl;
        private String cancelUrl;
    }

    @Data
    public static class TestConfig {
        private boolean enabled;
        private String description = "测试环境模拟支付";
    }
}
