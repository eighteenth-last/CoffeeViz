package com.coffeeviz.dto;

import lombok.Data;

/**
 * 管理端支付配置VO
 */
@Data
public class AdminPaymentConfigVO {

    private WechatPayConfig wechatPay;
    private AlipayConfig alipay;
    private TestConfig test;

    @Data
    public static class WechatPayConfig {
        private Boolean enabled;
        private String mchId;
        private String appId;
        private String apiKey;
        private String apiV3Key;
        private String privateKey;
        private String certSerialNo;
        private String notifyUrl;
    }

    @Data
    public static class AlipayConfig {
        private Boolean enabled;
        private String appId;
        private String privateKey;
        private String publicKey;
        private String signType;
        private String charset;
        private String gatewayUrl;
        private String notifyUrl;
        private String returnUrl;
    }

    @Data
    public static class TestConfig {
        private Boolean enabled;
        private String description;
    }
}
