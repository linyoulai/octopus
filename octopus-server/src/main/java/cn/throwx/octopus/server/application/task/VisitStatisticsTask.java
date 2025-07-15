package cn.throwx.octopus.server.application.task;

import cn.throwx.octopus.server.infra.common.LockKey;
import cn.throwx.octopus.server.infra.common.TimeZoneConstant;
import cn.throwx.octopus.server.infra.support.lock.DistributedLock;
import cn.throwx.octopus.server.infra.support.lock.DistributedLockFactory;
import cn.throwx.octopus.server.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1
 * @description 访问数据统计定时任务
 * @since 2020/12/26 17:09
 */
@RequiredArgsConstructor
@Component
public class VisitStatisticsTask {

    private final StatisticsService statisticsService;
    private final DistributedLockFactory distributedLockFactory;

    @Scheduled(cron = "0 0 1 * * ?") // 这里怎么设置成每天的凌晨一点启动任务？
    public void processVisitStatistics() {
        DistributedLock lock = distributedLockFactory.provideDistributedLock(LockKey.VISITOR_STATS_TASK.getCode());
        boolean tryLock = lock.tryLock(LockKey.VISITOR_STATS_TASK.getWaitTime(), LockKey.VISITOR_STATS_TASK.getReleaseTime(), TimeUnit.SECONDS);  // 怎么是trylock？获取不到锁就不统计数据了？
        if (tryLock) {
            try {
//                OffsetDateTime now = OffsetDateTime.now(TimeZoneConstant.CHINA.getZoneId());
//                OffsetDateTime start = now.minusDays(1L).withNano(0).withSecond(0).withMinute(0).withHour(0);
//                OffsetDateTime end = start.withNano(0).withSecond(59).withMinute(59).withHour(23);
//                statisticsService.processVisitStatisticsDuration(start, end);

                OffsetDateTime now = OffsetDateTime.now(TimeZoneConstant.CHINA.getZoneId());
// 将 start 设置为今天凌晨零点
                OffsetDateTime start = now.withNano(0).withSecond(0).withMinute(0).withHour(0);
// 将 end 设置为当前时间
                OffsetDateTime end = now;

                System.out.println("正在统计时间范围: " + start + " 到 " + end); // 加上日志方便调试
                statisticsService.processVisitStatisticsDuration(start, end);
            } finally {
                lock.unlock();
            }
        }
    }
}
