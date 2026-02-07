package com.coffeeviz.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.annotation.RateLimit;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * API 限流切面
 * 使用 Guava RateLimiter 实现令牌桶算法
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    
    /**
     * 存储每个限流 key 对应的 RateLimiter
     */
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Around("@annotation(com.coffeeviz.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        if (rateLimit != null) {
            String key = getCombineKey(rateLimit, point);
            
            // 获取或创建 RateLimiter
            RateLimiter rateLimiter = limiters.computeIfAbsent(key, k -> {
                // 计算每秒允许的请求数
                double permitsPerSecond = (double) rateLimit.count() / rateLimit.time();
                log.info("创建限流器: key={}, permitsPerSecond={}", k, permitsPerSecond);
                return RateLimiter.create(permitsPerSecond);
            });
            
            // 尝试获取令牌，等待最多 500ms
            boolean acquired = rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS);
            
            if (!acquired) {
                log.warn("请求被限流: key={}, method={}", key, method.getName());
                throw new IllegalStateException("访问过于频繁，请稍后再试");
            }
        }
        
        return point.proceed();
    }
    
    /**
     * 生成限流 key
     */
    private String getCombineKey(RateLimit rateLimit, ProceedingJoinPoint point) {
        StringBuilder key = new StringBuilder(rateLimit.key());
        
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            key.append(":").append(getIpAddress());
        } else if (rateLimit.limitType() == RateLimit.LimitType.USER) {
            try {
                Object loginId = StpUtil.getLoginIdDefaultNull();
                if (loginId != null) {
                    key.append(":").append(loginId);
                } else {
                    // 未登录用户使用 IP
                    key.append(":").append(getIpAddress());
                }
            } catch (Exception e) {
                // 获取用户 ID 失败，使用 IP
                key.append(":").append(getIpAddress());
            }
        }
        
        return key.toString();
    }
    
    /**
     * 获取客户端 IP 地址
     */
    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于多级代理，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
