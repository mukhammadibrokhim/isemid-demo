package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Pre-loads the region/district/neighborhood/country lookup caches so the
 * first real request never pays for populating them. Runs on the async
 * executor rather than the startup thread — the neighborhood table alone is
 * republic-wide (tens of thousands of rows), and blocking here would delay
 * the app becoming ready for no benefit: a request landing before warmup
 * finishes just falls through to the same synchronous {@code @Cacheable}
 * load this runner would otherwise have done inline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReferenceCacheWarmup implements ApplicationRunner {

    private final ReferenceCacheLoader cacheLoader;

    @Override
    @Async("applicationTaskExecutor")
    public void run(@NonNull ApplicationArguments args) {
        long startedAtNanos = System.nanoTime();

        cacheLoader.loadCountries();
        cacheLoader.loadRegions();
        cacheLoader.loadDistricts();
        cacheLoader.loadNeighborhoods();

        long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
        log.info("Reference lookup caches warmed up in {}ms", durationMs);
    }
}
