package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UsageQuota;
import com.coffeeviz.entity.UserSubscription;
import com.coffeeviz.mapper.UsageQuotaMapper;
import com.coffeeviz.service.QuotaService;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 配额服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {
    
    private final UsageQuotaMapper quotaMapper;
    private final SubscriptionService subscriptionService;
    
    @Override
    public boolean checkQuota(Long userId, String quotaType) {
        UsageQuota quota = getQuota(userId, quotaType);
        if (quota == null) {
            return false;
        }
        
        // 检查是否需要重置
        checkAndResetQuota(quota);
        
        // -1 表示无限制
        if (quota.getQuotaLimit() == -1) {
            return true;
        }
        
        return quota.getQuotaUsed() < quota.getQuotaLimit();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useQuota(Long userId, String quotaType) {
        UsageQuota quota = getQuota(userId, quotaType);
        if (quota == null) {
            log.error("配额不存在: userId={}, type={}", userId, quotaType);
            return false;
        }
        
        // 检查是否需要重置
        checkAndResetQuota(quota);
        
        // -1 表示无限制
        if (quota.getQuotaLimit() == -1) {
            return true;
        }
        
        if (quota.getQuotaUsed() >= quota.getQuotaLimit()) {
            log.warn("配额不足: userId={}, type={}, used={}, limit={}", 
                userId, quotaType, quota.getQuotaUsed(), quota.getQuotaLimit());
            return false;
        }
        
        quota.setQuotaUsed(quota.getQuotaUsed() + 1);
        quotaMapper.updateById(quota);
        
        log.info("使用配额: userId={}, type={}, used={}/{}", 
            userId, quotaType, quota.getQuotaUsed(), quota.getQuotaLimit());
        return true;
    }
    
    @Override
    public UsageQuota getQuota(Long userId, String quotaType) {
        return quotaMapper.selectOne(
            new LambdaQueryWrapper<UsageQuota>()
                .eq(UsageQuota::getUserId, userId)
                .eq(UsageQuota::getQuotaType, quotaType)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetQuota(Long userId, String quotaType) {
        UsageQuota quota = getQuota(userId, quotaType);
        if (quota == null) {
            return false;
        }
        
        quota.setQuotaUsed(0);
        quota.setLastResetTime(LocalDateTime.now());
        return quotaMapper.updateById(quota) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserQuota(Long userId) {
        log.info("初始化用户配额: userId={}", userId);
        
        // 获取用户订阅计划
        UserSubscription subscription = subscriptionService.getCurrentSubscription(userId);
        SubscriptionPlan plan = subscriptionService.getPlanByCode(
            subscription != null ? subscription.getPlanCode() : "FREE"
        );
        
        // 初始化各类配额
        createQuotaIfNotExists(userId, "repository", plan.getMaxRepositories(), "never");
        createQuotaIfNotExists(userId, "diagram", plan.getMaxDiagramsPerRepo(), "monthly");
        createQuotaIfNotExists(userId, "sql_parse", 100, "daily");
        createQuotaIfNotExists(userId, "ai_generate", plan.getSupportAi() == 1 ? 50 : 0, "monthly");
    }
    
    /**
     * 检查并重置配额
     */
    private void checkAndResetQuota(UsageQuota quota) {
        if ("never".equals(quota.getResetCycle())) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = quota.getLastResetTime();
        boolean shouldReset = false;
        
        switch (quota.getResetCycle()) {
            case "daily":
                shouldReset = lastReset.plusDays(1).isBefore(now);
                break;
            case "monthly":
                shouldReset = lastReset.plusMonths(1).isBefore(now);
                break;
            case "yearly":
                shouldReset = lastReset.plusYears(1).isBefore(now);
                break;
        }
        
        if (shouldReset) {
            quota.setQuotaUsed(0);
            quota.setLastResetTime(now);
            quotaMapper.updateById(quota);
            log.info("配额已重置: userId={}, type={}", quota.getUserId(), quota.getQuotaType());
        }
    }
    
    /**
     * 创建配额（如果不存在）
     */
    private void createQuotaIfNotExists(Long userId, String quotaType, Integer limit, String resetCycle) {
        UsageQuota existing = getQuota(userId, quotaType);
        if (existing != null) {
            return;
        }
        
        UsageQuota quota = new UsageQuota();
        quota.setUserId(userId);
        quota.setQuotaType(quotaType);
        quota.setQuotaLimit(limit);
        quota.setQuotaUsed(0);
        quota.setResetCycle(resetCycle);
        quota.setLastResetTime(LocalDateTime.now());
        
        quotaMapper.insert(quota);
    }
}
