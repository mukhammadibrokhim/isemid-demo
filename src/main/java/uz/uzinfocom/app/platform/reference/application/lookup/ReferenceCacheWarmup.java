package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Pre-loads the region/district/neighborhood/country lookup caches at startup
 * so the first real request never pays for populating them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReferenceCacheWarmup implements ApplicationRunner {

    private final ReferenceCacheLoader cacheLoader;

    @Override
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
