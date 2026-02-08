package com.coffeeviz.service;

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
     * 取消订阅
     */
    boolean cancelSubscription(Long userId, String reason);
    
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
}
