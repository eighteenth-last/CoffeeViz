package com.coffeeviz.annotation;

import java.lang.annotation.*;

/**
 * 需要订阅权限注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireSubscription {
    
    /**
     * 需要的功能特性
     */
    String feature() default "";
    
    /**
     * 需要的最低计划
     */
    String minPlan() default "FREE";
}
