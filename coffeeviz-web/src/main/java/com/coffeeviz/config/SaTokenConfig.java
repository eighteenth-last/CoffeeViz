package com.coffeeviz.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
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
                    "/api/ai/generate/stream",  // SSE 流式接口，在 Controller 内部验证 token
                    "/api/team/join/*/info",  // 邀请链接信息（公开接口）
                    "/api/team/register-and-join",  // 注册并加入团队（公开接口）
                    "/error",
                    "/favicon.ico"
                )
                .check(r -> StpUtil.checkLogin());
            
            // 管理端接口 - 需要 admin 角色
            SaRouter.match("/api/admin/**")
                .check(r -> StpUtil.checkRole("admin"));
            
        }))
        .addPathPatterns("/**")
        .excludePathPatterns("/static/**", "/public/**");
    }
    
    /**
     * 配置异步请求超时时间（与 SseEmitter 超时一致）
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置为 3 分钟，与 AiController 中 SseEmitter 超时一致
        configurer.setDefaultTimeout(180000L);
    }
}
