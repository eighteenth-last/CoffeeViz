package com.coffeeviz.annotation;

import java.lang.annotation.*;

/**
 * API 限流注解
 * 用于控制接口的访问频率
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流 key 前缀
     */
    String key() default "rate_limit";
    
    /**
     * 时间窗口（秒）
     */
    int time() default 60;
    
    /**
     * 时间窗口内最大请求次数
     */
    int count() default 100;
    
    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认策略：全局限流
         */
        DEFAULT,
        
        /**
         * 根据 IP 限流
         */
        IP,
        
        /**
         * 根据用户 ID 限流
         */
        USER
    }
}
