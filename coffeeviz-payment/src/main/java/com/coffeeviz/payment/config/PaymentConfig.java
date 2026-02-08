package com.coffeeviz.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    private WechatConfig wechat = new WechatConfig();
    private AlipayConfig alipay = new AlipayConfig();
    private StripeConfig stripe = new StripeConfig();
    
    @Data
    public static class WechatConfig {
        private String appId;
        private String mchId;
        private String apiKey;
        private String certPath;
        private String notifyUrl;
    }
    
    @Data
    public static class AlipayConfig {
        private String appId;
        private String privateKey;
        private String publicKey;
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
}
