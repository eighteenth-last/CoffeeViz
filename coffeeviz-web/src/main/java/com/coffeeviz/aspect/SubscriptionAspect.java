package com.coffeeviz.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.annotation.RequireSubscription;
import com.coffeeviz.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 订阅权限切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SubscriptionAspect {
    
    private final SubscriptionService subscriptionService;
    
    @Around("@annotation(com.coffeeviz.annotation.RequireSubscription)")
    public Object checkSubscription(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = StpUtil.getLoginIdAsLong();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireSubscription annotation = method.getAnnotation(RequireSubscription.class);
        
        // 检查订阅是否有效
        if (!subscriptionService.isSubscriptionValid(userId)) {
            throw new RuntimeException("订阅已过期，请续费");
        }
        
        // 检查功能权限
        String feature = annotation.feature();
        if (!feature.isEmpty() && !subscriptionService.hasFeature(userId, feature)) {
            throw new RuntimeException("当前订阅计划不支持此功能，请升级");
        }
        
        return joinPoint.proceed();
    }
}
