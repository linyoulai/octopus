package cn.throwx.octopus.server.filter;


import cn.throwx.octopus.server.cache.BloomFilterManager;
import cn.throwx.octopus.server.cache.UrlMapCacheManager;
import cn.throwx.octopus.server.infra.common.TransformStatus;
import cn.throwx.octopus.server.infra.exception.RedirectToErrorPageException;
import cn.throwx.octopus.server.model.entity.UrlMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * @author throwable
 * @description 短链转换过滤器
 * @since 2020/7/21 17:55
 */
@Slf4j
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class UrlTransformFilter implements TransformFilter {

    @Autowired
    private UrlMapCacheManager urlMapCacheManager;

    @Autowired
    private BloomFilterManager bloomFilterManager; // 注入布隆过滤器管理器

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void init(TransformContext context) {

    }

    @Override
    public void doFilter(TransformFilterChain chain,
                         TransformContext context) {
        String compressionCode = context.getCompressionCode();

        // 【新增】第一道防线：布隆过滤器校验
        if (!bloomFilterManager.mightContain(compressionCode)) {
            // 如果布隆过滤器说“绝对不存在”，直接中断链条并抛出异常
            log.warn("布隆过滤器拦截到无效压缩码: {}", compressionCode);
            throw new RedirectToErrorPageException(String.format("Invalid code: %s", compressionCode));
        }
        log.info("开始执行UrlTransformFilter,压缩码[{}]......", compressionCode);

        UrlMap urlMap = urlMapCacheManager.loadUrlMapCacheByCompressCode(compressionCode);
        context.setTransformStatus(TransformStatus.TRANSFORM_FAIL);
        if (Objects.nonNull(urlMap)) {
            context.setTransformStatus(TransformStatus.TRANSFORM_SUCCESS);
            context.setParam(TransformContext.PARAM_LONG_URL_KEY, urlMap.getLongUrl());
            context.setParam(TransformContext.PARAM_SHORT_URL_KEY, urlMap.getShortUrl());
            chain.doFilter(context);
        } else {
            log.warn("压缩码[{}]不存在或异常,终止TransformFilterChain执行,并且重定向到404页面......", compressionCode);
            throw new RedirectToErrorPageException(String.format("[c:%s]", compressionCode));
        }
    }
}
