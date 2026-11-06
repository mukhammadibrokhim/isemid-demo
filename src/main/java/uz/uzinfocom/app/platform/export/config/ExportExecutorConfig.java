package uz.uzinfocom.app.platform.export.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uz.uzinfocom.app.platform.observability.TraceIdTaskDecorator;

import java.util.concurrent.RejectedExecutionException;

/**
 * Dedicated, deliberately small pool for background Excel exports. Each running
 * export holds a JDBC connection for its whole streaming read (and briefly a
 * second one for every REQUIRES_NEW progress write), so running them on the
 * shared {@code applicationTaskExecutor} (up to 32 threads) against a 20-connection
 * Hikari pool let a burst of exports starve ordinary API requests of connections.
 * Capping concurrency here keeps the pool's headroom for interactive traffic;
 * extra jobs wait in a bounded queue, and past that the submit is rejected (503).
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class ExportExecutorConfig {

    public static final String EXPORT_TASK_EXECUTOR = "exportTaskExecutor";

    @Bean(name = EXPORT_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor exportTaskExecutor(ExportProperties properties, TraceIdTaskDecorator traceIdTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getMaxConcurrentJobs());
        executor.setMaxPoolSize(properties.getMaxConcurrentJobs());
        executor.setQueueCapacity(properties.getMaxQueuedJobs());
        executor.setThreadNamePrefix("export-");
        executor.setTaskDecorator(traceIdTaskDecorator);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler((task, pool) -> {
            log.error("event=export_task_rejected activeCount={} queuedTaskCount={}",
                    pool.getActiveCount(), pool.getQueue().size());
            throw new RejectedExecutionException("Export executor is saturated");
        });
        return executor;
    }
}
