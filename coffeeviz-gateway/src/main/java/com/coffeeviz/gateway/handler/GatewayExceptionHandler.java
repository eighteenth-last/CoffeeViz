package com.coffeeviz.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器
 * <p>
 * 捕获网关层的异常（路由不可达、服务不可用等），返回统一 JSON 格式。
 * </p>
 *
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 已经提交的响应不再处理
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        int code;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            code = rse.getStatusCode().value();
            message = switch (code) {
                case 404 -> "服务接口不存在";
                case 503 -> "服务暂时不可用，请稍后重试";
                default -> rse.getReason() != null ? rse.getReason() : "网关异常";
            };
        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            message = "网关内部错误: " + ex.getMessage();
        }

        log.error("[Gateway] 异常: {} - {}", code, message, ex);

        response.setStatusCode(HttpStatus.valueOf(code));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("msg", message);
        body.put("data", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + code + ",\"msg\":\"" + message + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
