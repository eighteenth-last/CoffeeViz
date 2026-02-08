package com.coffeeviz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 
 * @author CoffeeViz Team
 * @since 2.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 确保 /api/** 路径优先匹配 Controller 而不是静态资源
        configurer.setUseTrailingSlashMatch(true);
    }
}
