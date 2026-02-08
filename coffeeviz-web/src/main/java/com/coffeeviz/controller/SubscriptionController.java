package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UserSubscription;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * 获取所有订阅计划
     */
    @GetMapping("/plans")
    public Result<List<SubscriptionPlan>> getPlans() {
        List<SubscriptionPlan> plans = subscriptionService.getAllPlans();
        return Result.success(plans);
    }
    
    /**
     * 获取当前用户订阅
     */
    @GetMapping("/current")
    public Result<UserSubscription> getCurrentSubscription() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserSubscription subscription = subscriptionService.getCurrentSubscription(userId);
        return Result.success(subscription);
    }
    
    /**
     * 取消订阅
     */
    @PostMapping("/cancel")
    public Result<Void> cancelSubscription(@RequestParam(required = false) String reason) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = subscriptionService.cancelSubscription(userId, reason);
        return success ? Result.success() : Result.error("取消订阅失败");
    }
    
    /**
     * 检查功能权限
     */
    @GetMapping("/check-feature")
    public Result<Boolean> checkFeature(@RequestParam String feature) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean hasFeature = subscriptionService.hasFeature(userId, feature);
        return Result.success(hasFeature);
    }
}
