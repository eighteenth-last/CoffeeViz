package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.entity.UserQuotaTracking;
import com.coffeeviz.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 配额管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/quota")
@RequiredArgsConstructor
public class QuotaController {
    
    private final QuotaService quotaService;
    private final com.coffeeviz.mapper.QuotaUsageLogMapper quotaUsageLogMapper;
    
    /**
     * 获取用户所有配额信息
     */
    @GetMapping("/list")
    public Result<Map<String, UserQuotaTracking>> getUserQuotas() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            Map<String, UserQuotaTracking> quotas = new HashMap<>();
            
            // 获取各类配额
            String[] quotaTypes = {"repository", "diagram", "sql_parse", "ai_generate"};
            for (String type : quotaTypes) {
                UserQuotaTracking quota = quotaService.getUserQuotaTracking(userId, type);
                if (quota != null) {
                    quotas.put(type, quota);
                }
            }
            
            return Result.success(quotas);
            
        } catch (Exception e) {
            log.error("获取配额信息失败", e);
            return Result.error("获取配额信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定类型的配额信息
     */
    @GetMapping("/{quotaType}")
    public Result<UserQuotaTracking> getQuota(@PathVariable String quotaType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            UserQuotaTracking quota = quotaService.getUserQuotaTracking(userId, quotaType);
            
            if (quota == null) {
                return Result.error(404, "配额不存在");
            }
            
            return Result.success(quota);
            
        } catch (Exception e) {
            log.error("获取配额信息失败", e);
            return Result.error("获取配额信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查配额是否足够
     */
    @GetMapping("/check/{quotaType}")
    public Result<Boolean> checkQuota(@PathVariable String quotaType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            boolean sufficient = quotaService.hasAvailableQuota(userId, quotaType);
            return Result.success(sufficient);
            
        } catch (Exception e) {
            log.error("检查配额失败", e);
            return Result.error("检查配额失败: " + e.getMessage());
        }
    }

    /**
     * 额度使用记录（分页）
     */
    @GetMapping("/usage-logs")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.coffeeviz.entity.QuotaUsageLog>> getUsageLogs(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "quotaType", required = false) String quotaType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.coffeeviz.entity.QuotaUsageLog> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(com.coffeeviz.entity.QuotaUsageLog::getUserId, userId);
            if (quotaType != null && !quotaType.isEmpty()) {
                wrapper.eq(com.coffeeviz.entity.QuotaUsageLog::getQuotaType, quotaType);
            }
            wrapper.orderByDesc(com.coffeeviz.entity.QuotaUsageLog::getCreateTime);
            
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.coffeeviz.entity.QuotaUsageLog> pageParam =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            
            com.baomidou.mybatisplus.core.metadata.IPage<com.coffeeviz.entity.QuotaUsageLog> result =
                    quotaUsageLogMapper.selectPage(pageParam, wrapper);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取额度使用记录失败", e);
            return Result.error("获取额度使用记录失败: " + e.getMessage());
        }
    }
}
