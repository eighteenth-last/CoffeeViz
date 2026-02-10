package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.*;
import com.coffeeviz.exception.QuotaExceededException;
import com.coffeeviz.mapper.PlanQuotaMapper;
import com.coffeeviz.mapper.UserQuotaTrackingMapper;
import com.coffeeviz.service.QuotaService;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配额服务实现 V2
 * 支持新的计划配额和用户配额跟踪
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Slf4j
@Service("quotaServiceV2")
@org.springframework.context.annotation.Primary
@RequiredArgsConstructor
public class QuotaServiceImplV2 implements QuotaService {
    
    private final PlanQuotaMapper planQuotaMapper;
    private final UserQuotaTrackingMapper userQuotaTrackingMapper;
    private final SubscriptionService subscriptionService;
    
    // ========== 旧方法实现（使用新系统）==========
    
    @Override
    public boolean checkQuota(Long userId, String quotaType) {
        return hasAvailableQuota(userId, quotaType);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useQuota(Long userId, String quotaType) {
        try {
            consumeQuota(userId, quotaType, 1);
            return true;
        } catch (QuotaExceededException e) {
            log.warn("配额不足: {}", e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            log.error("配额不存在: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    @Deprecated
    public UsageQuota getQuota(Long userId, String quotaType) {
        log.warn("getQuota() 方法已废弃，请使用 getUserQuotaTracking()");
        // 返回 null，强制使用新方法
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetQuota(Long userId, String quotaType) {
        UserQuotaTracking tracking = getUserQuotaTracking(userId, quotaType);
        if (tracking == null) {
            log.warn("用户配额跟踪不存在: userId={}, quotaType={}", userId, quotaType);
            return false;
        }
        
        tracking.setQuotaUsed(0);
        tracking.setLastResetTime(LocalDateTime.now());
        return userQuotaTrackingMapper.updateById(tracking) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserQuota(Long userId) {
        log.info("初始化用户配额（旧方法）: userId={}", userId);
        
        UserSubscription subscription = subscriptionService.getCurrentSubscription(userId);
        if (subscription != null) {
            initializeUserQuotas(userId, subscription.getPlanId());
        } else {
            // 使用 FREE 计划初始化
            SubscriptionPlan freePlan = subscriptionService.getPlanByCode("FREE");
            if (freePlan != null) {
                initializeUserQuotas(userId, freePlan.getId());
            }
        }
    }

    
    // ========== 新方法实现 ==========
    
    @Override
    public List<PlanQuota> getPlanQuotas(Long planId) {
        return planQuotaMapper.selectList(
            new LambdaQueryWrapper<PlanQuota>()
                .eq(PlanQuota::getPlanId, planId)
                .orderByAsc(PlanQuota::getQuotaType)
        );
    }
    
    @Override
    public PlanQuota getPlanQuota(Long planId, String quotaType) {
        return planQuotaMapper.selectOne(
            new LambdaQueryWrapper<PlanQuota>()
                .eq(PlanQuota::getPlanId, planId)
                .eq(PlanQuota::getQuotaType, quotaType)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeUserQuotas(Long userId, Long planId) {
        log.info("初始化用户配额: userId={}, planId={}", userId, planId);
        
        // 获取计划的所有配额配置
        List<PlanQuota> planQuotas = getPlanQuotas(planId);
        
        if (planQuotas.isEmpty()) {
            log.warn("计划没有配额配置: planId={}", planId);
            return;
        }
        
        // 为每个计划配额创建用户跟踪记录
        for (PlanQuota planQuota : planQuotas) {
            // 检查是否已存在
            UserQuotaTracking existing = getUserQuotaTracking(userId, planQuota.getQuotaType());
            if (existing != null) {
                log.debug("用户配额跟踪已存在: userId={}, quotaType={}", userId, planQuota.getQuotaType());
                continue;
            }
            
            UserQuotaTracking tracking = new UserQuotaTracking();
            tracking.setUserId(userId);
            tracking.setPlanQuotaId(planQuota.getId());
            tracking.setQuotaType(planQuota.getQuotaType());
            tracking.setQuotaLimit(planQuota.getQuotaLimit());
            tracking.setQuotaUsed(0);
            tracking.setLastResetTime(LocalDateTime.now());
            
            userQuotaTrackingMapper.insert(tracking);
            log.info("创建用户配额跟踪: userId={}, quotaType={}, limit={}", 
                userId, planQuota.getQuotaType(), planQuota.getQuotaLimit());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserQuotaLimits(Long userId, Long newPlanId) {
        log.info("更新用户配额限制: userId={}, newPlanId={}", userId, newPlanId);
        
        // 获取新计划的所有配额配置
        List<PlanQuota> newPlanQuotas = getPlanQuotas(newPlanId);
        
        for (PlanQuota planQuota : newPlanQuotas) {
            UserQuotaTracking tracking = getUserQuotaTracking(userId, planQuota.getQuotaType());
            
            if (tracking == null) {
                // 如果不存在，创建新的跟踪记录
                tracking = new UserQuotaTracking();
                tracking.setUserId(userId);
                tracking.setPlanQuotaId(planQuota.getId());
                tracking.setQuotaType(planQuota.getQuotaType());
                tracking.setQuotaLimit(planQuota.getQuotaLimit());
                tracking.setQuotaUsed(0);
                tracking.setLastResetTime(LocalDateTime.now());
                userQuotaTrackingMapper.insert(tracking);
                log.info("创建新配额跟踪: userId={}, quotaType={}, limit={}", 
                    userId, planQuota.getQuotaType(), planQuota.getQuotaLimit());
            } else {
                // 更新限制，保留已使用的值
                Integer oldLimit = tracking.getQuotaLimit();
                tracking.setPlanQuotaId(planQuota.getId());
                tracking.setQuotaLimit(planQuota.getQuotaLimit());
                userQuotaTrackingMapper.updateById(tracking);
                log.info("更新配额限制: userId={}, quotaType={}, oldLimit={}, newLimit={}, used={}", 
                    userId, planQuota.getQuotaType(), oldLimit, planQuota.getQuotaLimit(), tracking.getQuotaUsed());
            }
        }
    }
    
    @Override
    public boolean hasAvailableQuota(Long userId, String quotaType) {
        UserQuotaTracking tracking = getUserQuotaTracking(userId, quotaType);
        if (tracking == null) {
            log.warn("用户配额跟踪不存在: userId={}, quotaType={}", userId, quotaType);
            return false;
        }
        
        // -1 表示无限制
        if (tracking.getQuotaLimit() == -1) {
            return true;
        }
        
        return tracking.getQuotaUsed() < tracking.getQuotaLimit();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(Long userId, String quotaType, int amount) {
        UserQuotaTracking tracking = getUserQuotaTracking(userId, quotaType);
        if (tracking == null) {
            throw new IllegalStateException("用户配额跟踪不存在: userId=" + userId + ", quotaType=" + quotaType);
        }
        
        // -1 表示无限制
        if (tracking.getQuotaLimit() == -1) {
            log.debug("无限制配额，跳过消耗: userId={}, quotaType={}", userId, quotaType);
            return;
        }
        
        int newUsed = tracking.getQuotaUsed() + amount;
        if (newUsed > tracking.getQuotaLimit()) {
            throw new QuotaExceededException(quotaType, tracking.getQuotaUsed(), tracking.getQuotaLimit());
        }
        
        tracking.setQuotaUsed(newUsed);
        userQuotaTrackingMapper.updateById(tracking);
        
        log.info("消耗配额: userId={}, quotaType={}, amount={}, used={}/{}", 
            userId, quotaType, amount, newUsed, tracking.getQuotaLimit());
    }
    
    @Override
    public UserQuotaTracking getUserQuotaTracking(Long userId, String quotaType) {
        return userQuotaTrackingMapper.selectOne(
            new LambdaQueryWrapper<UserQuotaTracking>()
                .eq(UserQuotaTracking::getUserId, userId)
                .eq(UserQuotaTracking::getQuotaType, quotaType)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserQuotas(Long userId) {
        log.info("重置用户所有配额: userId={}", userId);
        
        List<UserQuotaTracking> trackings = userQuotaTrackingMapper.selectList(
            new LambdaQueryWrapper<UserQuotaTracking>()
                .eq(UserQuotaTracking::getUserId, userId)
        );
        
        LocalDateTime now = LocalDateTime.now();
        for (UserQuotaTracking tracking : trackings) {
            tracking.setQuotaUsed(0);
            tracking.setLastResetTime(now);
            userQuotaTrackingMapper.updateById(tracking);
        }
        
        log.info("已重置 {} 个配额", trackings.size());
    }
}
