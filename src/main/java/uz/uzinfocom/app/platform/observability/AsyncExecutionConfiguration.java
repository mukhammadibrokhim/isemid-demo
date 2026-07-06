package uz.uzinfocom.app.platform.observability;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;

@Configuration(proxyBeanMethods = false)
@EnableAsync
@RequiredArgsConstructor
public class AsyncExecutionConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncExecutionConfiguration.class);

    private final ObservabilityProperties properties;
    private final TraceIdTaskDecorator traceIdTaskDecorator;

    @Bean(name = {"applicationTaskExecutor", "taskExecutor"})
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ObservabilityProperties.AsyncExecutor config = properties.getAsyncExecutor();
        validate(config);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setTaskDecorator(traceIdTaskDecorator);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds());
        executor.setRejectedExecutionHandler((task, pool) -> {
            LOGGER.error(
                    "Async task rejected. traceId={}, poolSize={}, activeCount={}, queueSize={}, completedTaskCount={}",
                    TraceContext.currentTraceId(),
                    pool.getPoolSize(),
                    pool.getActiveCount(),
                    pool.getQueue().size(),
                    pool.getCompletedTaskCount()
            );
            throw new RejectedExecutionException("Application async executor is saturated");
        });
        return executor;
    }

    private void validate(ObservabilityProperties.AsyncExecutor config) {
        if (config.getMaxPoolSize() < config.getCorePoolSize()) {
            throw new IllegalStateException(
                    "app.observability.async-executor.max-pool-size must be greater than or equal to core-pool-size"
            );
        }
        if (config.getQueueCapacity() <= 0) {
            throw new IllegalStateException(
                    "app.observability.async-executor.queue-capacity must be positive"
            );
        }
    }
}
