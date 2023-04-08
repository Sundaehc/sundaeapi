package com.sundae.sundaeapigateway;

import com.sundae.sundaeapicommon.model.entity.InterfaceInfo;
import com.sundae.sundaeapicommon.model.entity.User;
import com.sundae.sundaeapicommon.service.InnerInterfaceInfoService;
import com.sundae.sundaeapicommon.service.InnerUserInterfaceInfoService;
import com.sundae.sundaeapicommon.service.InnerUserService;
import com.sundae.sundaeclientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 全局过滤
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    public static final String INTERFACE_PATH = "http://localhost:8121";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_PATH + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识:" + request.getId());
        log.info("请求路径:" + path);
        log.info("请求方法:" + method);
        log.info("请求参数:" + request.getQueryParams());
        String sourceAddress = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        log.info("请求来源地址:" + sourceAddress);
        ServerHttpResponse response = exchange.getResponse();
        // 黑白名单
//        if (!IP_WHITE_LIST.contains(sourceAddress)) {
//            response.setStatusCode(HttpStatus.FORBIDDEN);
//            return response.setComplete();
//        }
        HttpHeaders headers = request.getHeaders();
        // 用户鉴权
        String accessKey = headers.getFirst("accessKey");
        String nonce =  headers.getFirst("nonce");
        String timeStamp =  headers.getFirst("timeStamp");
        String sign =  headers.getFirst("sign");
        String body =  headers.getFirst("body");
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error");
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        if (Long.parseLong(nonce) > 10000L) {
            return handleNoAuth(response);
        }
        // 时间戳和当前时间不能超过5分钟
        long currentTime = System.currentTimeMillis() / 1000;
        final long FIVE_MINUTES = 60 * 5L;
        if ( (currentTime - Long.parseLong(timeStamp)) >= FIVE_MINUTES) {
            return handleNoAuth(response);
        }
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            throw new RuntimeException("无权限");
        }
        // 从数据库中查询模拟接口是否存在,以及请求方法是否匹配
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error");
        }
        if (interfaceInfo == null) {
            return handleNoAuth(response);
        }
        // 请求转发，调用模拟接口 + 响应日志
       return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                            handleInvokeError(originalResponse);
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}
