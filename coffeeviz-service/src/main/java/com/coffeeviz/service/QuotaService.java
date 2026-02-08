package com.coffeeviz.service;

import com.coffeeviz.entity.UsageQuota;

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
     * 获取配额信息
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
}
