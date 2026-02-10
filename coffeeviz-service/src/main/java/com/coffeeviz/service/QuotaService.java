package com.coffeeviz.service;

import com.coffeeviz.entity.PlanQuota;
import com.coffeeviz.entity.UsageQuota;
import com.coffeeviz.entity.UserQuotaTracking;

import java.util.List;

/**
 * 配额服务接口
 */
public interface QuotaService {
    
    /**
     * 检查配额是否足够
     */
    boolean checkQuota(Long userId, String quotaType);
    
    /**
     * 使用配额
     */
    boolean useQuota(Long userId, String quotaType);
    
    /**
     * 获取配额信息（旧方法，保持向后兼容）
     */
    UsageQuota getQuota(Long userId, String quotaType);
    
    /**
     * 重置配额
     */
    boolean resetQuota(Long userId, String quotaType);
    
    /**
     * 初始化用户配额
     */
    void initUserQuota(Long userId);
    
    // ========== 新增方法 ==========
    
    /**
     * 获取订阅计划的所有配额配置
     */
    List<PlanQuota> getPlanQuotas(Long planId);
    
    /**
     * 根据计划ID和配额类型获取特定计划配额
     */
    PlanQuota getPlanQuota(Long planId, String quotaType);
    
    /**
     * 根据计划配额初始化用户配额跟踪
     */
    void initializeUserQuotas(Long userId, Long planId);
    
    /**
     * 当计划更改时更新用户配额限制
     */
    void updateUserQuotaLimits(Long userId, Long newPlanId);
    
    /**
     * 检查用户是否有特定类型的可用配额
     */
    boolean hasAvailableQuota(Long userId, String quotaType);
    
    /**
     * 消耗用户配额
     */
    void consumeQuota(Long userId, String quotaType, int amount);
    
    /**
     * 获取用户当前配额使用情况
     */
    UserQuotaTracking getUserQuotaTracking(Long userId, String quotaType);
    
    /**
     * 根据重置周期重置用户配额
     */
    void resetUserQuotas(Long userId);
}
