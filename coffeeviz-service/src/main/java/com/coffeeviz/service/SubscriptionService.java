package com.coffeeviz.service;

import com.coffeeviz.dto.SubscriptionCreateRequest;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UserSubscription;

import java.util.List;

/**
 * 订阅服务接口
 */
public interface SubscriptionService {
    
    /**
     * 获取所有订阅计划
     */
    List<SubscriptionPlan> getAllPlans();
    
    /**
     * 根据代码获取订阅计划
     */
    SubscriptionPlan getPlanByCode(String planCode);
    
    /**
     * 获取用户当前订阅
     */
    UserSubscription getCurrentSubscription(Long userId);
    
    /**
     * 创建订阅
     */
    UserSubscription createSubscription(Long userId, Long planId, String billingCycle);
    
    /**
     * 升级订阅
     */
    UserSubscription upgradeSubscription(Long userId, Long newPlanId, String billingCycle);
    
    /**
     * 取消用户订阅（旧方法）
     * @deprecated 使用 {@link #cancelSubscription(Long, String)} 替代
     */
    @Deprecated
    boolean cancelUserSubscription(Long userId, String reason);
    
    /**
     * 续费订阅
     */
    UserSubscription renewSubscription(Long userId);
    
    /**
     * 检查订阅是否有效
     */
    boolean isSubscriptionValid(Long userId);
    
    /**
     * 检查功能权限
     */
    boolean hasFeature(Long userId, String feature);


    /**
     * 根据ID获取订阅计划
     */
    SubscriptionPlan getPlanById(Long planId);

    /**
     * 获取用户当前有效订阅
     */
    UserSubscription getUserActiveSubscription(Long userId);
    
    // ========== 新增方法 ==========
    
    /**
     * 创建带支付验证的新订阅
     */
    UserSubscription createSubscription(SubscriptionCreateRequest request);
    
    /**
     * 验证订阅的支付订单
     * @throws com.coffeeviz.exception.PaymentVerificationException 如果验证失败
     */
    void verifyPayment(Long paymentOrderId, Long planId, String billingCycle);
    
    /**
     * 支付验证后激活订阅
     */
    UserSubscription activateSubscription(Long subscriptionId, Long paymentOrderId);
    
    /**
     * 升级或降级用户订阅
     */
    UserSubscription changeSubscriptionPlan(Long userId, Long newPlanId, String billingCycle);
    
    /**
     * 获取用户的活动订阅
     */
    UserSubscription getActiveSubscription(Long userId);
    
    /**
     * 取消订阅
     *
     * @return
     */
    boolean cancelSubscription(Long subscriptionId, String reason);

}
