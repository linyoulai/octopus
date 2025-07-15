package cn.throwx.octopus.server.cache;

import cn.throwx.octopus.server.repository.UrlMapDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterManager implements CommandLineRunner {

    private final RedissonClient redissonClient;
    private final UrlMapDao urlMapDao;

    // 定义布隆过滤器的 Redis Key
    private static final String BLOOM_FILTER_KEY = "octopus:bloom_filter:compression_code";

    // 获取布隆过滤器对象
    private RBloomFilter<String> getBloomFilter() {
        return redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
    }
    
    // **A. 服务启动时，全量加载**
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化短码布隆过滤器...");
        RBloomFilter<String> bloomFilter = getBloomFilter();
        
        // 初始化布隆过滤器，预估1亿元素，期望误判率0.1%
        // tryInit 方法只有在过滤器不存在时才会创建，是幂等的
        bloomFilter.tryInit(100_000_000L, 0.001);

        // 从数据库加载已有短码并添加到过滤器中
        // 注意：生产环境如果数据量巨大，这里需要分批加载
        urlMapDao.selectAll().forEach(urlMap -> bloomFilter.add(urlMap.getCompressionCode()));
        
        log.info("短码布隆过滤器初始化完成，当前元素数量: {}", bloomFilter.count());
    }

    // **B. 增量更新**
    public void add(String compressionCode) {
        getBloomFilter().add(compressionCode);
    }

    // **C. 存在性判断**
    public boolean mightContain(String compressionCode) {
        return getBloomFilter().contains(compressionCode);
    }
}