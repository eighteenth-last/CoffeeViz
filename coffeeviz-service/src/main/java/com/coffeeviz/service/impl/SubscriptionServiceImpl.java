package com.coffeeviz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.SubscriptionPlan;
import com.coffeeviz.entity.UserSubscription;
import com.coffeeviz.mapper.SubscriptionPlanMapper;
import com.coffeeviz.mapper.UserSubscriptionMapper;
import com.coffeeviz.service.QuotaService;
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
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionPlanMapper planMapper;
    private final UserSubscriptionMapper subscriptionMapper;
    private final com.coffeeviz.mapper.TeamMemberMapper teamMemberMapper;
    private final com.coffeeviz.mapper.TeamMapper teamMapper;
    private QuotaService quotaService;
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    
    public SubscriptionServiceImpl(SubscriptionPlanMapper planMapper, 
                                   UserSubscriptionMapper subscriptionMapper,
                                   com.coffeeviz.mapper.TeamMemberMapper teamMemberMapper,
                                   com.coffeeviz.mapper.TeamMapper teamMapper) {
        this.planMapper = planMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.teamMapper = teamMapper;
    }
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    public void setQuotaService(QuotaService quotaService) {
        this.quotaService = quotaService;
    }
    
    @org.springframework.beans.factory.annotation.Autowired
    public void setEventPublisher(org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
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
        
        LocalDateTime now = LocalDateTime.now();
        UserSubscription current = getCurrentSubscription(userId);
        
        // 同计划续费：在现有到期时间基础上累加，不取消旧订阅
        if (current != null && "active".equals(current.getStatus())
                && current.getPlanId().equals(planId)
                && current.getEndTime().isAfter(now)) {
            log.info("同计划续费，时间累加: userId={}, planId={}, 当前到期={}", userId, planId, current.getEndTime());
            LocalDateTime newEndTime = "yearly".equals(billingCycle)
                    ? current.getEndTime().plusYears(1)
                    : current.getEndTime().plusMonths(1);
            current.setEndTime(newEndTime);
            subscriptionMapper.updateById(current);
            // 续费也发送订阅邮件
            try {
                eventPublisher.publishEvent(new com.coffeeviz.event.SubscriptionCreatedEvent(
                        this, userId, plan.getPlanName(), billingCycle, newEndTime.toString()));
            } catch (Exception e) {
                log.warn("发布续费事件失败: {}", e.getMessage());
            }
            return current;
        }
        
        // 不同计划（升级/降级）或无活跃订阅：取消旧订阅，创建新订阅
        // 不同计划时，将旧订阅剩余时间作为新订阅的起始偏移
        LocalDateTime startTime = now;
        if (current != null && "active".equals(current.getStatus())) {
            current.setStatus("cancelled");
            current.setCancelTime(now);
            current.setCancelReason("升级到新计划");
            subscriptionMapper.updateById(current);
            // 如果旧订阅还没过期，新订阅从当前时间开始（不累加不同计划的剩余时间）
        }
        
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
        
        // 使用 QuotaService 初始化用户配额
        quotaService.initializeUserQuotas(userId, planId);
        
        // 发布订阅成功事件（非 FREE 计划才发邮件）
        if (!"FREE".equalsIgnoreCase(plan.getPlanCode())) {
            try {
                eventPublisher.publishEvent(new com.coffeeviz.event.SubscriptionCreatedEvent(
                        this, userId, plan.getPlanName(), billingCycle, endTime.toString()));
            } catch (Exception e) {
                log.warn("发布订阅事件失败，不影响订阅流程: {}", e.getMessage());
            }
        }
        
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
    public boolean cancelUserSubscription(Long userId, String reason) {
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
        if (subscription != null && "active".equals(subscription.getStatus()) 
                && subscription.getEndTime().isAfter(LocalDateTime.now())) {
            return true;
        }
        
        // 如果用户自身订阅无效，检查是否是团队成员（继承团队 owner 的订阅）
        Long ownerId = resolveTeamOwnerId(userId);
        if (ownerId != null && !ownerId.equals(userId)) {
            UserSubscription ownerSubscription = getCurrentSubscription(ownerId);
            if (ownerSubscription != null && "active".equals(ownerSubscription.getStatus())
                    && ownerSubscription.getEndTime().isAfter(LocalDateTime.now())) {
                log.debug("团队订阅共享: userId={} 继承 ownerId={} 的订阅", userId, ownerId);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean hasFeature(Long userId, String feature) {
        UserSubscription subscription = getCurrentSubscription(userId);
        if (subscription != null) {
            SubscriptionPlan plan = planMapper.selectById(subscription.getPlanId());
            if (plan != null && checkPlanFeature(plan, feature)) {
                return true;
            }
        }
        
        // 如果用户自身计划不支持该功能，检查是否是团队成员
        Long ownerId = resolveTeamOwnerId(userId);
        if (ownerId != null && !ownerId.equals(userId)) {
            UserSubscription ownerSubscription = getCurrentSubscription(ownerId);
            if (ownerSubscription != null) {
                SubscriptionPlan ownerPlan = planMapper.selectById(ownerSubscription.getPlanId());
                if (ownerPlan != null && checkPlanFeature(ownerPlan, feature)) {
                    log.debug("团队功能共享: userId={} 继承 ownerId={} 的 {} 功能", userId, ownerId, feature);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查计划是否支持指定功能
     */
    private boolean checkPlanFeature(SubscriptionPlan plan, String feature) {
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
     * 解析团队 owner ID（团队订阅共享）
     * 如果用户是某个活跃团队的成员（非 owner），返回团队 owner 的 ID。
     * 否则返回 null。
     */
    private Long resolveTeamOwnerId(Long userId) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.coffeeviz.entity.TeamMember> memberQuery = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            memberQuery.eq(com.coffeeviz.entity.TeamMember::getUserId, userId)
                    .eq(com.coffeeviz.entity.TeamMember::getStatus, "active")
                    .eq(com.coffeeviz.entity.TeamMember::getRole, "member");
            
            java.util.List<com.coffeeviz.entity.TeamMember> memberships = teamMemberMapper.selectList(memberQuery);
            
            if (memberships == null || memberships.isEmpty()) {
                return null;
            }
            
            for (com.coffeeviz.entity.TeamMember membership : memberships) {
                com.coffeeviz.entity.Team team = teamMapper.selectById(membership.getTeamId());
                if (team != null && "active".equals(team.getStatus())) {
                    return team.getOwnerId();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.warn("解析团队 owner 失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }
    
    @Override
    public SubscriptionPlan getPlanById(Long planId) {
        return planMapper.selectById(planId);
    }

    @Override
    public UserSubscription getUserActiveSubscription(Long userId) {
        return getCurrentSubscription(userId);
    }
    
    // ========== 新方法的空实现（待完成）==========
    
    @Override
    public UserSubscription createSubscription(com.coffeeviz.dto.SubscriptionCreateRequest request) {
        throw new UnsupportedOperationException("待实现");
    }
    
    @Override
    public void verifyPayment(Long paymentOrderId, Long planId, String billingCycle) {
        throw new UnsupportedOperationException("待实现");
    }
    
    @Override
    public UserSubscription activateSubscription(Long subscriptionId, Long paymentOrderId) {
        throw new UnsupportedOperationException("待实现");
    }
    
    @Override
    public UserSubscription changeSubscriptionPlan(Long userId, Long newPlanId, String billingCycle) {
        return upgradeSubscription(userId, newPlanId, billingCycle);
    }
    
    @Override
    public UserSubscription getActiveSubscription(Long userId) {
        return getCurrentSubscription(userId);
    }
    
    @Override
    public boolean cancelSubscription(Long subscriptionId, String reason) {
        throw new UnsupportedOperationException("待实现");
    }
}
