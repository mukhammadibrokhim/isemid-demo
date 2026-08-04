package uz.uzinfocom.isemid.features.report.job;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Form058 export V3 (fon jarayonida disk fayl + kesh) uchun infratuzilma:
 * - jobCache: 5 daqiqa TTL, muddati tugagan/almashtirilgan yozuv o'chganda diskdagi eski fayl ham o'chiriladi.
 * - executor: bir vaqtning o'zida faqat 1 ta generatsiya ishlaydi (CPU/DB yukini past ushlash uchun).
 */
@Slf4j
@Configuration
@EnableAsync
public class Form058ExportJobCacheConfig {

    @Bean(name = "form058ExportJobCache")
    public Cache<String, Form058ExportJob> form058ExportJobCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .scheduler(Scheduler.systemScheduler())
                .removalListener((String key, Form058ExportJob job, RemovalCause cause) -> {
                    if (job == null || job.getFilePath() == null) {
                        return;
                    }
                    try {
                        boolean deleted = Files.deleteIfExists(job.getFilePath());
                        if (deleted) {
                            log.info("[Form058ExportV3] eski fayl o'chirildi ({}): {}", cause, job.getFilePath());
                        }
                    } catch (IOException e) {
                        log.warn("[Form058ExportV3] faylni o'chirib bo'lmadi: {}", job.getFilePath(), e);
                    }
                })
                .build();
    }

    @Bean(name = "form058ExportJobExecutor")
    public Executor form058ExportJobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("form058-export-v3-");
        executor.initialize();
        return executor;
    }
}
