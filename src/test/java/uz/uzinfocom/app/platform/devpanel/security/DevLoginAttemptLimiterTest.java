package uz.uzinfocom.app.platform.devpanel.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevLoginAttemptLimiterTest {

    private final DevLoginAttemptLimiter limiter = new DevLoginAttemptLimiter();

    @Test
    void blocksAUsernameAfterTooManyFailures() {
        for (int i = 0; i < DevLoginAttemptLimiter.MAX_FAILURES_PER_USERNAME; i++) {
            assertThat(limiter.isBlocked("dev-admin", "203.0.113." + i)).isFalse();
            limiter.recordFailure("dev-admin", "203.0.113." + i);
        }

        assertThat(limiter.isBlocked("DEV-ADMIN", "198.51.100.1")).isTrue();
        assertThat(limiter.isBlocked("someone-else", "198.51.100.1")).isFalse();
    }

    @Test
    void blocksAnIpSprayingManyUsernames() {
        for (int i = 0; i < DevLoginAttemptLimiter.MAX_FAILURES_PER_IP; i++) {
            limiter.recordFailure("user-" + i, "203.0.113.9");
        }

        assertThat(limiter.isBlocked("fresh-user", "203.0.113.9")).isTrue();
        assertThat(limiter.isBlocked("fresh-user", "203.0.113.10")).isFalse();
    }

    @Test
    void successClearsTheUsernameCounter() {
        for (int i = 0; i < DevLoginAttemptLimiter.MAX_FAILURES_PER_USERNAME - 1; i++) {
            limiter.recordFailure("dev-admin", null);
        }
        limiter.recordSuccess("dev-admin");
        limiter.recordFailure("dev-admin", null);

        assertThat(limiter.isBlocked("dev-admin", null)).isFalse();
    }
}
