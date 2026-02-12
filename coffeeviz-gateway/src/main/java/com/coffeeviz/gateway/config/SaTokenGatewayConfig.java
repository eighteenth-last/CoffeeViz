package com.coffeeviz.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 网关鉴权配置
 * <p>
 * 在网关层统一拦截所有请求，校验登录状态。
 * 白名单内的接口（登录、注册、公开资源等）直接放行。
 * </p>
 *
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Configuration
public class SaTokenGatewayConfig {

    /**
     * 注册 Sa-Token 全局过滤器（响应式）
     */
    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有路由
                .addInclude("/**")
                // 放行静态资源
                .addExclude("/favicon.ico", "/static/**", "/public/**")
                // 鉴权逻辑
                .setAuth(obj -> {
                    // 所有接口默认需要登录
                    SaRouter.match("/**")
                            .notMatch(
                                    // ===== 认证相关（公开） =====
                                    "/api/auth/login",
                                    "/api/auth/register",
                                    "/api/auth/logout",
                                    "/api/auth/sms/send",
                                    "/api/auth/sms/login",
                                    "/api/auth/wechat/**",
                                    // ===== 公开资源 =====
                                    "/api/image/**",
                                    "/api/ai/generate/stream",
                                    // ===== 团队公开接口 =====
                                    "/api/team/join/*/info",
                                    "/api/team/register-and-join",
                                    // ===== 支付回调（第三方调用） =====
                                    "/api/payment/notify/**",
                                    // ===== Actuator 健康检查 =====
                                    "/actuator/**",
                                    "/error"
                            )
                            .check(r -> StpUtil.checkLogin());
                })
                // 异常处理
                .setError(e -> {
                    if (e instanceof NotLoginException) {
                        return "{\"code\":401,\"msg\":\"未登录或 Token 已过期，请重新登录\",\"data\":null}";
                    }
                    return "{\"code\":500,\"msg\":\"网关鉴权异常: " + e.getMessage() + "\",\"data\":null}";
                });
    }
}
