package com.coffeeviz.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验 - 拦截所有路由，排除登录注册等接口
            SaRouter.match("/**")
                .notMatch(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/logout",
                    "/api/auth/sms/send",
                    "/api/auth/sms/login",
                    "/api/auth/wechat/**",
                    "/api/image/**",  // 图片代理接口不需要认证
                    "/error",
                    "/favicon.ico"
                )
                .check(r -> StpUtil.checkLogin());
            
        }))
        .addPathPatterns("/**")
        .excludePathPatterns("/static/**", "/public/**");
    }
}
