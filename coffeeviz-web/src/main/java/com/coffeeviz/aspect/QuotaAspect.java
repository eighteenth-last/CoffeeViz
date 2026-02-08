package com.coffeeviz.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.annotation.RequireQuota;
import com.coffeeviz.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 配额检查切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class QuotaAspect {
    
    private final QuotaService quotaService;
    
    @Around("@annotation(com.coffeeviz.annotation.RequireQuota)")
    public Object checkQuota(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = StpUtil.getLoginIdAsLong();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireQuota annotation = method.getAnnotation(RequireQuota.class);
        
        String quotaType = annotation.value();
        
        // 检查配额
        if (!quotaService.checkQuota(userId, quotaType)) {
            throw new RuntimeException("配额不足，请升级订阅计划");
        }
        
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 使用配额
        quotaService.useQuota(userId, quotaType);
        
        return result;
    }
}
