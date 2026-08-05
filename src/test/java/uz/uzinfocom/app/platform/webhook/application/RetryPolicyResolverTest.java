package uz.uzinfocom.app.platform.webhook.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetryPolicyResolverTest {

    private final SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);
    private final RetryPolicyResolver resolver = new RetryPolicyResolver(systemSettingResolver);

    @Test
    void defaultBackoffScheduleIsFiveStepsMatchingTheDocumentedDefault() {
        when(systemSettingResolver.resolveStringList(eq("webhook.dispatch.backoff-schedule"), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        List<Duration> schedule = resolver.backoffSchedule();

        assertThat(schedule).containsExactly(
                Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(15),
                Duration.ofHours(1), Duration.ofHours(6)
        );
        assertThat(resolver.maxAttempts()).isEqualTo(5);
    }

    @Test
    void nextDelayReturnsTheScheduledDurationForEachAttemptWithinBounds() {
        when(systemSettingResolver.resolveStringList(eq("webhook.dispatch.backoff-schedule"), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        assertThat(resolver.nextDelay(1)).contains(Duration.ofMinutes(1));
        assertThat(resolver.nextDelay(2)).contains(Duration.ofMinutes(5));
        assertThat(resolver.nextDelay(5)).contains(Duration.ofHours(6));
    }

    @Test
    void nextDelayIsEmptyOnceEveryScheduledAttemptIsUsed() {
        when(systemSettingResolver.resolveStringList(eq("webhook.dispatch.backoff-schedule"), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        assertThat(resolver.nextDelay(6)).isEmpty();
        assertThat(resolver.nextDelay(0)).isEmpty();
    }

    @Test
    void customScheduleFromSettingsOverridesTheDefaultAndDrivesMaxAttempts() {
        when(systemSettingResolver.resolveStringList(eq("webhook.dispatch.backoff-schedule"), any()))
                .thenReturn(List.of("PT30S", "PT2M"));

        assertThat(resolver.backoffSchedule()).containsExactly(Duration.ofSeconds(30), Duration.ofMinutes(2));
        assertThat(resolver.maxAttempts()).isEqualTo(2);
        assertThat(resolver.nextDelay(1)).contains(Duration.ofSeconds(30));
        assertThat(resolver.nextDelay(2)).contains(Duration.ofMinutes(2));
        assertThat(resolver.nextDelay(3)).isEmpty();
    }

    @Test
    void malformedScheduleFallsBackToTheDefault() {
        when(systemSettingResolver.resolveStringList(eq("webhook.dispatch.backoff-schedule"), any()))
                .thenReturn(List.of("not-a-duration", "also-bad"));

        assertThat(resolver.maxAttempts()).isEqualTo(5);
    }

    @Test
    void batchSizeDefaultsToFiftyAndIsAtLeastOne() {
        when(systemSettingResolver.resolveLong(eq("webhook.dispatch.batch-size"), anyLong())).thenReturn(50L);
        assertThat(resolver.batchSize()).isEqualTo(50);

        when(systemSettingResolver.resolveLong(eq("webhook.dispatch.batch-size"), anyLong())).thenReturn(0L);
        assertThat(resolver.batchSize()).isEqualTo(1);
    }
}
