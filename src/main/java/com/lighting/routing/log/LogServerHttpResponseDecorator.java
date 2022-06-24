package com.lighting.routing.log;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class LogServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    private static final String AUTHORIZE_TOKEN = "token";

    private LocalDateTime startDateTime;

    private ServerWebExchange exchange;

    public LogServerHttpResponseDecorator(ServerWebExchange exchange) {
        super(exchange.getResponse());
        startDateTime = LocalDateTime.now();
        this.exchange = exchange;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if (body instanceof Flux) {
            Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
            return super.writeWith(fluxBody.map(dataBuffer -> {
                DataBufferFactory bufferFactory = getDelegate().bufferFactory();
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                DataBufferUtils.release(dataBuffer);
                byte[] uppedContent = new String(content, Charset.forName("UTF-8")).getBytes();
                writeLog(content);
                return bufferFactory.wrap(uppedContent);
            }));
        }
        return super.writeWith(body);
    }

    private void writeLog(byte[] content) {
        try {
            ApiLog apiLog = new ApiLog();
            apiLog.setStartDateTime(startDateTime);
            apiLog.setEndDateTime(LocalDateTime.now());
            apiLog.setDuration(Duration.between(startDateTime, LocalDateTime.now()).toMillis());
            apiLog.setRequestBody(parseRequestBody(exchange));
            apiLog.setResponseBody(new String(content, Charset.forName("UTF-8")));
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

    private void writeLog(ApiLog apiLog) {
        log.info(JSONObject.toJSONString(apiLog));
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
