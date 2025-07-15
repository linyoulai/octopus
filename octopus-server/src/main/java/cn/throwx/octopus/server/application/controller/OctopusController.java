package cn.throwx.octopus.server.application.controller;

import cn.throwx.octopus.server.filter.TransformContext;
import cn.throwx.octopus.server.service.UrlMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;

/**
 * @author throwable
 * @version v1
 * @description
 * @since 2020/12/26 19:15
 */
@RequiredArgsConstructor // RequiredArgsConstructor 和 AllArgsConstructor 有什么区别？//只对final和@NonNull的属性
@RestController // RestController和Controller有什么区别？返回JSON
public class OctopusController { // controller包和api包有什么区别？不都是对外开放接口吗？controller包面向最终用户、前端用户，处理HTTP请求；api包面向后台用户，处理RPC调用。

    private final UrlMapService urlMapService;

    @GetMapping(path = "/{compressionCode}") // 域名是什么？用户访问的完整的url是什么？域名由配置文件决定，http://localhost:9099/aBcDeF
    @ResponseStatus(HttpStatus.FOUND) // ResponseStatus注解是什么？为什么要设置为FOUND？设置HTTP响应默认状态码为302.@PathVariable(name = "compressionCode")这是啥？这跟{}内的compressionCode是一个意思，就为了映射。ServerWebExchange这又是啥?一次HTTP请求和响应的所有信息。
    public Mono<Void> dispatch(@PathVariable(name = "compressionCode") String compressionCode, ServerWebExchange exchange) { // mono是啥玩意，泛型怎么是Void？void大写V我还是第一次见
        ServerHttpRequest request = exchange.getRequest(); // getRequest？
        TransformContext context = new TransformContext(); // 转换上下文？是啥？
        context.setCompressionCode(compressionCode);
        context.setParam(TransformContext.PARAM_SERVER_WEB_EXCHANGE_KEY, exchange); // ？？？
        if (Objects.nonNull(request.getRemoteAddress())) { // request.getRemoteAddress()是啥？为什么要判断非空？
            context.setParam(TransformContext.PARAM_REMOTE_HOST_NAME_KEY, request.getRemoteAddress().getHostName());
        }
        HttpHeaders httpHeaders = request.getHeaders();
        Set<String> headerNames = httpHeaders.keySet();
        if (!CollectionUtils.isEmpty(headerNames)) {
            headerNames.forEach(headerName -> {
                String headerValue = httpHeaders.getFirst(headerName);
                context.setHeader(headerName, headerValue);
            });
        }
        // 处理转换
        urlMapService.processTransform(context);
        // 这里有一个技巧,flush用到的线程和内部逻辑处理的线程不是同一个线程,所有要用到TTL
        return Mono.fromRunnable(context.getRedirectAction());
    }
}





















