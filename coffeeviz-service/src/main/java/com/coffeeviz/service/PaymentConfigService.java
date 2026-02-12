package com.coffeeviz.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.SysConfigGroup;
import com.coffeeviz.mapper.SysConfigGroupMapper;
import com.coffeeviz.payment.config.PaymentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 支付配置服务 - 从数据库加载支付配置并刷新到 PaymentConfig
 * 每种支付方式独立一条记录：payment_wechat / payment_alipay / payment_test
 * status 字段控制是否启用（1=启用 0=禁用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfigService {

    private final SysConfigGroupMapper configGroupMapper;
    private final PaymentConfig paymentConfig;
    private final com.coffeeviz.payment.handler.AlipayPaymentHandler alipayPaymentHandler;
    private final com.coffeeviz.payment.handler.WechatPaymentHandler wechatPaymentHandler;

    @PostConstruct
    public void init() {
        refreshPaymentConfig();
    }

    /**
     * 从数据库刷新支付配置到 PaymentConfig Bean
     */
    public void refreshPaymentConfig() {
        try {
            List<SysConfigGroup> groups = configGroupMapper.selectList(
                    new LambdaQueryWrapper<SysConfigGroup>()
                            .likeRight(SysConfigGroup::getGroupCode, "payment_"));

            for (SysConfigGroup group : groups) {
                String code = group.getGroupCode();
                boolean enabled = group.getStatus() != null && group.getStatus() == 1;
                JSONObject json = (group.getConfigValue() != null)
                        ? JSON.parseObject(group.getConfigValue()) : new JSONObject();

                switch (code) {
                    case "payment_wechat" -> loadWechatConfig(json, enabled);
                    case "payment_alipay" -> loadAlipayConfig(json, enabled);
                    case "payment_test" -> loadTestConfig(json, enabled);
                }
            }

            log.info("支付配置已从数据库刷新: wechat={}, alipay={}, test={}",
                    paymentConfig.getWechat().isEnabled(),
                    paymentConfig.getAlipay().isEnabled(),
                    paymentConfig.getTest().isEnabled());

            // 清除支付客户端缓存，使新配置生效
            try {
                alipayPaymentHandler.clearCache();
                wechatPaymentHandler.clearCache();
            } catch (Exception e) {
                log.warn("清除支付客户端缓存失败", e);
            }
        } catch (Exception e) {
            log.error("刷新支付配置失败", e);
        }
    }

    private void loadWechatConfig(JSONObject json, boolean enabled) {
        PaymentConfig.WechatConfig wechat = paymentConfig.getWechat();
        wechat.setEnabled(enabled);
        if (json.getString("appId") != null) wechat.setAppId(json.getString("appId"));
        if (json.getString("mchId") != null) wechat.setMchId(json.getString("mchId"));
        if (json.getString("apiKey") != null) wechat.setApiKey(json.getString("apiKey"));
        if (json.getString("apiV3Key") != null) wechat.setApiV3Key(json.getString("apiV3Key"));
        if (json.getString("privateKey") != null) wechat.setPrivateKey(json.getString("privateKey"));
        if (json.getString("certSerialNo") != null) wechat.setCertSerialNo(json.getString("certSerialNo"));
        if (json.getString("notifyUrl") != null) wechat.setNotifyUrl(json.getString("notifyUrl"));
    }

    private void loadAlipayConfig(JSONObject json, boolean enabled) {
        PaymentConfig.AlipayConfig alipay = paymentConfig.getAlipay();
        alipay.setEnabled(enabled);
        if (json.getString("appId") != null) alipay.setAppId(json.getString("appId"));
        if (json.getString("privateKey") != null) alipay.setPrivateKey(json.getString("privateKey"));
        if (json.getString("publicKey") != null) alipay.setPublicKey(json.getString("publicKey"));
        if (json.getString("signType") != null) alipay.setSignType(json.getString("signType"));
        if (json.getString("charset") != null) alipay.setCharset(json.getString("charset"));
        if (json.getString("gatewayUrl") != null) alipay.setGatewayUrl(json.getString("gatewayUrl"));
        if (json.getString("notifyUrl") != null) alipay.setNotifyUrl(json.getString("notifyUrl"));
        if (json.getString("returnUrl") != null) alipay.setReturnUrl(json.getString("returnUrl"));
    }

    private void loadTestConfig(JSONObject json, boolean enabled) {
        PaymentConfig.TestConfig test = paymentConfig.getTest();
        test.setEnabled(enabled);
        if (json.getString("description") != null) test.setDescription(json.getString("description"));
    }
}
