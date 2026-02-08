package com.coffeeviz.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.UsageQuota;
import com.coffeeviz.mapper.UsageQuotaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配额重置定时任务
 * 
 * @author CoffeeViz Team
 * @since 1.2.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaResetTask {
    
    private final UsageQuotaMapper quotaMapper;
    
    /**
     * 每天凌晨 1 点重置每日配额
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetDailyQuota() {
        log.info("开始重置每日配额");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            
            // 查询需要重置的每日配额
            List<UsageQuota> quotas = quotaMapper.selectList(
                new LambdaQueryWrapper<UsageQuota>()
                    .eq(UsageQuota::getResetCycle, "daily")
                    .lt(UsageQuota::getLastResetTime, yesterday)
            );
            
            int resetCount = 0;
            for (UsageQuota quota : quotas) {
                quota.setQuotaUsed(0);
                quota.setLastResetTime(now);
                quotaMapper.updateById(quota);
                resetCount++;
            }
            
            log.info("每日配额重置完成，重置数量: {}", resetCount);
            
        } catch (Exception e) {
            log.error("重置每日配额失败", e);
        }
    }
    
    /**
     * 每月 1 日凌晨 2 点重置每月配额
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void resetMonthlyQuota() {
        log.info("开始重置每月配额");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastMonth = now.minusMonths(1);
            
            // 查询需要重置的每月配额
            List<UsageQuota> quotas = quotaMapper.selectList(
                new LambdaQueryWrapper<UsageQuota>()
                    .eq(UsageQuota::getResetCycle, "monthly")
                    .lt(UsageQuota::getLastResetTime, lastMonth)
            );
            
            int resetCount = 0;
            for (UsageQuota quota : quotas) {
                quota.setQuotaUsed(0);
                quota.setLastResetTime(now);
                quotaMapper.updateById(quota);
                resetCount++;
            }
            
            log.info("每月配额重置完成，重置数量: {}", resetCount);
            
        } catch (Exception e) {
            log.error("重置每月配额失败", e);
        }
    }
    
    /**
     * 每年 1 月 1 日凌晨 3 点重置每年配额
     */
    @Scheduled(cron = "0 0 3 1 1 ?")
    public void resetYearlyQuota() {
        log.info("开始重置每年配额");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastYear = now.minusYears(1);
            
            // 查询需要重置的每年配额
            List<UsageQuota> quotas = quotaMapper.selectList(
                new LambdaQueryWrapper<UsageQuota>()
                    .eq(UsageQuota::getResetCycle, "yearly")
                    .lt(UsageQuota::getLastResetTime, lastYear)
            );
            
            int resetCount = 0;
            for (UsageQuota quota : quotas) {
                quota.setQuotaUsed(0);
                quota.setLastResetTime(now);
                quotaMapper.updateById(quota);
                resetCount++;
            }
            
            log.info("每年配额重置完成，重置数量: {}", resetCount);
            
        } catch (Exception e) {
            log.error("重置每年配额失败", e);
        }
    }
}
