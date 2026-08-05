package uz.uzinfocom.app.platform.webhook.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Wraps {@link SystemSettingResolver} for the two outbound-webhook retry
 * tunables, re-read on every call so a dev-panel edit takes effect on the
 * very next poll without a redeploy - same "resolver, not static config"
 * pattern as {@code DynamicCircuitBreakerLookup}.
 *
 * <p>{@code maxAttempts} is derived from the backoff schedule's length rather
 * than configured separately: attempt N's delay before attempt N+1 is
 * {@code schedule[N-1]}, and once N reaches the schedule's length there is no
 * more delay to apply, so the dispatch is {@code EXHAUSTED}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryPolicyResolver {

    private static final String KEY_BACKOFF_SCHEDULE = "webhook.dispatch.backoff-schedule";
    private static final String KEY_BATCH_SIZE = "webhook.dispatch.batch-size";

    private static final List<String> DEFAULT_BACKOFF_SCHEDULE_RAW =
            List.of("PT1M", "PT5M", "PT15M", "PT1H", "PT6H");
    private static final List<Duration> DEFAULT_BACKOFF_SCHEDULE =
            DEFAULT_BACKOFF_SCHEDULE_RAW.stream().map(Duration::parse).toList();
    private static final long DEFAULT_BATCH_SIZE = 50;

    private final SystemSettingResolver systemSettingResolver;

    public List<Duration> backoffSchedule() {
        List<String> raw = systemSettingResolver.resolveStringList(KEY_BACKOFF_SCHEDULE, DEFAULT_BACKOFF_SCHEDULE_RAW);
        List<Duration> parsed = raw.stream()
                .map(RetryPolicyResolver::parseDurationOrNull)
                .filter(Objects::nonNull)
                .toList();

        if (parsed.isEmpty()) {
            log.warn("event=webhook_backoff_schedule_parse_failure raw={} - falling back to the default schedule", raw);
            return DEFAULT_BACKOFF_SCHEDULE;
        }
        return parsed;
    }

    public int maxAttempts() {
        return backoffSchedule().size();
    }

    public int batchSize() {
        long value = systemSettingResolver.resolveLong(KEY_BATCH_SIZE, DEFAULT_BATCH_SIZE);
        return (int) Math.max(1, value);
    }

    /**
     * The delay to apply before the next attempt, given the attempt count
     * that just failed (1-based - the count already includes the failed
     * attempt). Empty once every scheduled retry has been used, meaning the
     * dispatch should be marked {@code EXHAUSTED} instead of rescheduled.
     */
    public Optional<Duration> nextDelay(int failedAttemptCount) {
        List<Duration> schedule = backoffSchedule();
        if (failedAttemptCount < 1 || failedAttemptCount > schedule.size()) {
            return Optional.empty();
        }
        return Optional.of(schedule.get(failedAttemptCount - 1));
    }

    private static Duration parseDurationOrNull(String value) {
        try {
            return Duration.parse(value.trim());
        } catch (DateTimeParseException | NullPointerException invalid) {
            return null;
        }
    }
}
