package com.coffeeviz.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 安全响应头过滤器
 * <p>
 * 为所有响应添加安全相关的 HTTP 头，防御 XSS、点击劫持、MIME 嗅探等攻击。
 * </p>
 *
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Component
public class XssProtectionFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            // 防止 XSS
            headers.addIfAbsent("X-XSS-Protection", "1; mode=block");
            // 防止 MIME 类型嗅探
            headers.addIfAbsent("X-Content-Type-Options", "nosniff");
            // 防止点击劫持
            headers.addIfAbsent("X-Frame-Options", "DENY");
            // 控制 Referrer 信息泄露
            headers.addIfAbsent("Referrer-Policy", "strict-origin-when-cross-origin");
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
