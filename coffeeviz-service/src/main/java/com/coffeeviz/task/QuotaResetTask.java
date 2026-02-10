package com.coffeeviz.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.entity.PlanQuota;
import com.coffeeviz.entity.UserQuotaTracking;
import com.coffeeviz.mapper.PlanQuotaMapper;
import com.coffeeviz.mapper.UserQuotaTrackingMapper;
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
    
    private final UserQuotaTrackingMapper quotaTrackingMapper;
    private final PlanQuotaMapper planQuotaMapper;
    
    /**
     * 每天凌晨 1 点重置每日配额
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetDailyQuota() {
        log.info("开始重置每日配额");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询所有每日重置周期的计划配额
            List<PlanQuota> planQuotas = planQuotaMapper.selectList(
                new LambdaQueryWrapper<PlanQuota>()
                    .eq(PlanQuota::getResetCycle, "daily")
            );
            
            if (planQuotas.isEmpty()) {
                log.info("没有需要重置的每日配额");
                return;
            }
            
            int resetCount = 0;
            for (PlanQuota planQuota : planQuotas) {
                // 查询使用该计划配额的所有用户跟踪记录
                List<UserQuotaTracking> trackings = quotaTrackingMapper.selectList(
                    new LambdaQueryWrapper<UserQuotaTracking>()
                        .eq(UserQuotaTracking::getPlanQuotaId, planQuota.getId())
                );
                
                for (UserQuotaTracking tracking : trackings) {
                    tracking.setQuotaUsed(0);
                    tracking.setLastResetTime(now);
                    quotaTrackingMapper.updateById(tracking);
                    resetCount++;
                }
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
            
            // 查询所有每月重置周期的计划配额
            List<PlanQuota> planQuotas = planQuotaMapper.selectList(
                new LambdaQueryWrapper<PlanQuota>()
                    .eq(PlanQuota::getResetCycle, "monthly")
            );
            
            if (planQuotas.isEmpty()) {
                log.info("没有需要重置的每月配额");
                return;
            }
            
            int resetCount = 0;
            for (PlanQuota planQuota : planQuotas) {
                // 查询使用该计划配额的所有用户跟踪记录
                List<UserQuotaTracking> trackings = quotaTrackingMapper.selectList(
                    new LambdaQueryWrapper<UserQuotaTracking>()
                        .eq(UserQuotaTracking::getPlanQuotaId, planQuota.getId())
                );
                
                for (UserQuotaTracking tracking : trackings) {
                    tracking.setQuotaUsed(0);
                    tracking.setLastResetTime(now);
                    quotaTrackingMapper.updateById(tracking);
                    resetCount++;
                }
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
            
            // 查询所有每年重置周期的计划配额
            List<PlanQuota> planQuotas = planQuotaMapper.selectList(
                new LambdaQueryWrapper<PlanQuota>()
                    .eq(PlanQuota::getResetCycle, "yearly")
            );
            
            if (planQuotas.isEmpty()) {
                log.info("没有需要重置的每年配额");
                return;
            }
            
            int resetCount = 0;
            for (PlanQuota planQuota : planQuotas) {
                // 查询使用该计划配额的所有用户跟踪记录
                List<UserQuotaTracking> trackings = quotaTrackingMapper.selectList(
                    new LambdaQueryWrapper<UserQuotaTracking>()
                        .eq(UserQuotaTracking::getPlanQuotaId, planQuota.getId())
                );
                
                for (UserQuotaTracking tracking : trackings) {
                    tracking.setQuotaUsed(0);
                    tracking.setLastResetTime(now);
                    quotaTrackingMapper.updateById(tracking);
                    resetCount++;
                }
            }
            
            log.info("每年配额重置完成，重置数量: {}", resetCount);
            
        } catch (Exception e) {
            log.error("重置每年配额失败", e);
        }
    }
}
