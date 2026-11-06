package uz.uzinfocom.app.platform.devpanel.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Brute-force brake for the dev panel's HTTP Basic login. Failed attempts
 * are counted per username and per client IP; once either crosses its
 * threshold, every further attempt for that key is refused without even
 * checking the password until the window (measured from the first failure)
 * expires. Per-IP stops one source from spraying many usernames, per-username
 * stops a distributed guess at one account. In-memory, per instance.
 */
@Component
public class DevLoginAttemptLimiter {

    static final int MAX_FAILURES_PER_USERNAME = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final Duration LOCK_WINDOW = Duration.ofMinutes(15);

    private final Cache<String, AtomicInteger> failuresByUsername = newCounterCache();
    private final Cache<String, AtomicInteger> failuresByIp = newCounterCache();

    public boolean isBlocked(String username, String ip) {
        return count(failuresByUsername, normalize(username)) >= MAX_FAILURES_PER_USERNAME
                || (ip != null && count(failuresByIp, ip) >= MAX_FAILURES_PER_IP);
    }

    public void recordFailure(String username, String ip) {
        failuresByUsername.get(normalize(username), ignored -> new AtomicInteger()).incrementAndGet();
        if (ip != null) {
            failuresByIp.get(ip, ignored -> new AtomicInteger()).incrementAndGet();
        }
    }

    public void recordSuccess(String username) {
        failuresByUsername.invalidate(normalize(username));
    }

    private static int count(Cache<String, AtomicInteger> cache, String key) {
        AtomicInteger counter = cache.getIfPresent(key);
        return counter == null ? 0 : counter.get();
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static Cache<String, AtomicInteger> newCounterCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(LOCK_WINDOW)
                .maximumSize(100_000)
                .build();
    }
}
