package uz.uzinfocom.app.platform.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Per-instance request budgets for the endpoints most exposed to abuse:
 * the public login-proxy, the public integration token endpoint, and the
 * citizen/legal-entity lookups (each call is a personal-data query against
 * API2). Counted in memory per app instance - a reverse-proxy limit in front
 * of the app is the cluster-wide complement, not a replacement.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    /** {@code /v1/auth/**} - keyed by client IP (callers are anonymous here). */
    private Rule auth = new Rule(20, Duration.ofMinutes(1));

    /** {@code /integration/v1/oauth/token} - keyed by client IP. */
    private Rule integrationToken = new Rule(20, Duration.ofMinutes(1));

    /** {@code /v1/citizen/**} + {@code /v1/legal-entity/**} - keyed by authenticated user. */
    private Rule identityLookup = new Rule(60, Duration.ofMinutes(1));

    @Getter
    @Setter
    public static class Rule {

        private int limit;

        private Duration window;

        public Rule() {
        }

        public Rule(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }
}
