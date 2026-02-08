package com.coffeeviz.annotation;

import java.lang.annotation.*;

/**
 * 需要配额注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireQuota {
    
    /**
     * 配额类型
     */
    String value();
}
