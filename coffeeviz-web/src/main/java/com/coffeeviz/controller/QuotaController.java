package com.coffeeviz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.common.Result;
import com.coffeeviz.entity.UsageQuota;
import com.coffeeviz.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
    
    /**
     * 获取用户所有配额信息
     */
    @GetMapping("/list")
    public Result<Map<String, UsageQuota>> getUserQuotas() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            Map<String, UsageQuota> quotas = new HashMap<>();
            
            // 获取各类配额
            String[] quotaTypes = {"repository", "diagram", "sql_parse", "ai_generate"};
            for (String type : quotaTypes) {
                UsageQuota quota = quotaService.getQuota(userId, type);
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
    public Result<UsageQuota> getQuota(@PathVariable String quotaType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            UsageQuota quota = quotaService.getQuota(userId, quotaType);
            
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
            boolean sufficient = quotaService.checkQuota(userId, quotaType);
            return Result.success(sufficient);
            
        } catch (Exception e) {
            log.error("检查配额失败", e);
            return Result.error("检查配额失败: " + e.getMessage());
        }
    }
}
