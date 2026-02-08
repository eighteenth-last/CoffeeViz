package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UsageQuota;
import com.coffeeviz.entity.UserSubscription;
import com.coffeeviz.mapper.SubscriptionPlanMapper;
import com.coffeeviz.mapper.UsageQuotaMapper;
import com.coffeeviz.mapper.UserSubscriptionMapper;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionPlanMapper planMapper;
    private final UserSubscriptionMapper subscriptionMapper;
    private final UsageQuotaMapper quotaMapper;
    
    @Override
    public List<SubscriptionPlan> getAllPlans() {
        return planMapper.selectList(
            new LambdaQueryWrapper<SubscriptionPlan>()
                .eq(SubscriptionPlan::getStatus, "active")
                .orderByAsc(SubscriptionPlan::getSortOrder)
        );
    }
    
    @Override
    public SubscriptionPlan getPlanByCode(String planCode) {
        return planMapper.selectOne(
            new LambdaQueryWrapper<SubscriptionPlan>()
                .eq(SubscriptionPlan::getPlanCode, planCode)
        );
    }
    
    @Override
    public UserSubscription getCurrentSubscription(Long userId) {
        return subscriptionMapper.selectOne(
            new LambdaQueryWrapper<UserSubscription>()
                .eq(UserSubscription::getUserId, userId)
                .eq(UserSubscription::getStatus, "active")
                .orderByDesc(UserSubscription::getCreateTime)
                .last("LIMIT 1")
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscription createSubscription(Long userId, Long planId, String billingCycle) {
        log.info("创建订阅: userId={}, planId={}, cycle={}", userId, planId, billingCycle);
        
        SubscriptionPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("订阅计划不存在");
        }
        
        // 取消当前订阅（如果存在）
        UserSubscription current = getCurrentSubscription(userId);
        if (current != null && "active".equals(current.getStatus())) {
            current.setStatus("cancelled");
            current.setCancelTime(LocalDateTime.now());
            current.setCancelReason("升级到新计划");
            subscriptionMapper.updateById(current);
        }
        
        // 计算结束时间
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = "yearly".equals(billingCycle) 
            ? startTime.plusYears(1) 
            : startTime.plusMonths(1);
        
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setPlanId(planId);
        subscription.setPlanCode(plan.getPlanCode());
        subscription.setBillingCycle(billingCycle);
        subscription.setPrice("yearly".equals(billingCycle) ? plan.getPriceYearly() : plan.getPriceMonthly());
        subscription.setStartTime(startTime);
        subscription.setEndTime(endTime);
        subscription.setAutoRenew(0);
        subscription.setStatus("active");
        
        subscriptionMapper.insert(subscription);
        
        // 更新用户配额
        updateUserQuota(userId, plan);
        
        return subscription;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscription upgradeSubscription(Long userId, Long newPlanId, String billingCycle) {
        log.info("升级订阅: userId={}, newPlanId={}", userId, newPlanId);
        
        // 取消当前订阅
        UserSubscription current = getCurrentSubscription(userId);
        if (current != null) {
            current.setStatus("cancelled");
            current.setCancelTime(LocalDateTime.now());
            current.setCancelReason("升级到新计划");
            subscriptionMapper.updateById(current);
        }
        
        // 创建新订阅
        return createSubscription(userId, newPlanId, billingCycle);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSubscription(Long userId, String reason) {
        log.info("取消订阅: userId={}, reason={}", userId, reason);
        
        UserSubscription subscription = getCurrentSubscription(userId);
        if (subscription == null) {
            return false;
        }
        
        subscription.setStatus("cancelled");
        subscription.setCancelTime(LocalDateTime.now());
        subscription.setCancelReason(reason);
        subscription.setAutoRenew(0);
        
        return subscriptionMapper.updateById(subscription) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscription renewSubscription(Long userId) {
        log.info("续费订阅: userId={}", userId);
        
        UserSubscription current = getCurrentSubscription(userId);
        if (current == null) {
            throw new IllegalStateException("没有找到当前订阅");
        }
        
        // 延长订阅时间
        LocalDateTime newEndTime = "yearly".equals(current.getBillingCycle())
            ? current.getEndTime().plusYears(1)
            : current.getEndTime().plusMonths(1);
        
        current.setEndTime(newEndTime);
        subscriptionMapper.updateById(current);
        
        return current;
    }
    
    @Override
    public boolean isSubscriptionValid(Long userId) {
        UserSubscription subscription = getCurrentSubscription(userId);
        if (subscription == null) {
            return false;
        }
        
        return "active".equals(subscription.getStatus()) 
            && subscription.getEndTime().isAfter(LocalDateTime.now());
    }
    
    @Override
    public boolean hasFeature(Long userId, String feature) {
        UserSubscription subscription = getCurrentSubscription(userId);
        if (subscription == null) {
            return false;
        }
        
        SubscriptionPlan plan = planMapper.selectById(subscription.getPlanId());
        if (plan == null) {
            return false;
        }
        
        // 根据功能检查权限
        switch (feature) {
            case "jdbc":
                return plan.getSupportJdbc() == 1;
            case "ai":
                return plan.getSupportAi() == 1;
            case "export":
                return plan.getSupportExport() == 1;
            case "team":
                return plan.getSupportTeam() == 1;
            default:
                return false;
        }
    }
    
    /**
     * 更新用户配额
     */
    private void updateUserQuota(Long userId, SubscriptionPlan plan) {
        log.info("更新用户配额: userId={}, plan={}", userId, plan.getPlanCode());
        
        // 更新 repository 配额
        updateQuota(userId, "repository", plan.getMaxRepositories(), "never");
        
        // 更新 diagram 配额
        updateQuota(userId, "diagram", plan.getMaxDiagramsPerRepo() * 10, "monthly");
        
        // 更新 sql_parse 配额
        updateQuota(userId, "sql_parse", 100, "daily");
        
        // 更新 ai_generate 配额
        int aiQuota = plan.getSupportAi() == 1 ? 50 : 0;
        updateQuota(userId, "ai_generate", aiQuota, "monthly");
    }
    
    /**
     * 更新单个配额
     */
    private void updateQuota(Long userId, String quotaType, Integer limit, String resetCycle) {
        UsageQuota quota = quotaMapper.selectOne(
            new LambdaQueryWrapper<UsageQuota>()
                .eq(UsageQuota::getUserId, userId)
                .eq(UsageQuota::getQuotaType, quotaType)
        );
        
        if (quota == null) {
            // 创建新配额
            quota = new UsageQuota();
            quota.setUserId(userId);
            quota.setQuotaType(quotaType);
            quota.setQuotaLimit(limit);
            quota.setQuotaUsed(0);
            quota.setResetCycle(resetCycle);
            quota.setLastResetTime(LocalDateTime.now());
            quotaMapper.insert(quota);
        } else {
            // 更新配额限制
            quota.setQuotaLimit(limit);
            quota.setResetCycle(resetCycle);
            quotaMapper.updateById(quota);
        }
    }
}
