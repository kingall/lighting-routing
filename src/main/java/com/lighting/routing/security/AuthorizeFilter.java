package com.lighting.routing.security;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighting.routing.log.ApiLog;
import com.lighting.routing.log.RoutingLogWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;


@Slf4j
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZE_TOKEN = "token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 不鉴权的访问地址
        if (request.getURI().getPath().contains("/admin/login")) {
            return chain.filter(exchange);
        }

        //token为空返回权限不足
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        if (!StringUtils.hasLength(token)) {
            return buildUnAuthorized(exchange);
        }

        //验证token有效性
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            return buildUnAuthorized(exchange);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private Mono<Void> buildUnAuthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        DataBufferFactory bufferFactory = response.bufferFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DataBuffer wrap = bufferFactory.wrap(objectMapper.writeValueAsBytes("{\"code\":403,\"msg\":\"权限不足!\"}"));
            writeLog(exchange);
            return response.writeWith(Mono.fromSupplier(() -> wrap));
        } catch (JsonProcessingException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return response.setComplete();
    }

    private void writeLog(ServerWebExchange exchange) {
        try {
            ApiLog apiLog = new ApiLog();
            apiLog.setStartDateTime(LocalDateTime.now());
            apiLog.setEndDateTime(LocalDateTime.now());
            apiLog.setDuration(0);
            apiLog.setRequestBody(parseRequestBody(exchange));
            apiLog.setResponseBody("{\"code\":403,\"msg\":\"权限不足!\"}");
            apiLog.setUri(exchange.getRequest().getURI().toString());

            String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZE_TOKEN);
            if (StringUtils.hasLength(token)) {
                apiLog.setToken(token);
            }
            RoutingLogWriter.writeRoutingLog(apiLog);
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
    }

    private String parseRequestBody(ServerWebExchange exchange){
        String method = exchange.getRequest().getMethodValue().toUpperCase();
        StringBuilder requestBody=new StringBuilder();
        if ("POST".equals(method)) {
            requestBody.append(exchange.getAttributeOrDefault("cachedRequestBody", ""));
        }
        return requestBody.toString();
    }
}
