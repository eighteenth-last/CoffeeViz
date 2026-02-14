package com.coffeeviz.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.coffeeviz.entity.ApiCallLog;
import com.coffeeviz.mapper.ApiCallLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API 调用日志切面
 * 拦截所有 Controller 方法，异步记录调用日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiCallLogAspect {

    private final ApiCallLogMapper apiCallLogMapper;

    @Around("execution(* com.coffeeviz.controller..*.*(..)) " +
            "&& !execution(* com.coffeeviz.controller.AdminController.getDashboardStats(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        int statusCode = 200;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            statusCode = 500;
            throw e;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                saveLogAsync(statusCode, duration);
            } catch (Exception e) {
                // 日志记录失败不影响主流程
            }
        }
    }

    private void saveLogAsync(int statusCode, long duration) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;

            HttpServletRequest request = attrs.getRequest();
            String path = request.getRequestURI();
            String method = request.getMethod();

            // 跳过静态资源和健康检查
            if (path.startsWith("/actuator") || path.contains("/favicon")) return;

            Long userId = null;
            try {
                userId = StpUtil.getLoginIdAsLong();
            } catch (Exception ignored) {}

            String clientIp = getClientIp(request);

            ApiCallLog logEntry = new ApiCallLog();
            logEntry.setUserId(userId);
            logEntry.setMethod(method);
            logEntry.setPath(path);
            logEntry.setStatusCode(statusCode);
            logEntry.setDuration(duration);
            logEntry.setClientIp(clientIp);
            logEntry.setCallDate(LocalDate.now());
            logEntry.setCreateTime(LocalDateTime.now());

            apiCallLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.debug("记录 API 调用日志失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
