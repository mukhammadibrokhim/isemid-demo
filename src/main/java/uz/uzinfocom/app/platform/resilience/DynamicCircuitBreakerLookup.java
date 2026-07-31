package uz.uzinfocom.app.platform.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single access point every outbound-HTTP client goes through to get its
 * {@link CircuitBreaker}, instead of calling {@link CircuitBreakerRegistry}
 * directly. On every lookup it re-resolves every tunable threshold -
 * failure-rate %, sliding-window size, minimum number of calls,
 * wait-duration-in-open-state, slow-call-rate %, slow-call-duration,
 * permitted-calls-in-half-open-state, and automatic-transition-to-half-open -
 * from {@link SystemSettingResolver} under keys
 * {@code resilience.circuitbreaker.<name>.<knob>}, editable at runtime from
 * the dev panel ({@code DevSettingController}, {@code /v1/dev/**}) without a
 * redeploy. {@code application.properties} stays the fallback: an empty or
 * untouched settings table behaves exactly as the static config already did.
 *
 * <p>resilience4j has no built-in "hot reload" for an already-created
 * breaker - each {@link CircuitBreaker} instance holds an immutable config
 * snapshot from the moment it was created. When a resolved value drifts from
 * what's currently applied, this pushes the new {@link CircuitBreakerConfig}
 * onto the registry's named config template (so anything created from now on
 * picks it up) and evicts every live instance built from that template (so
 * the very next call anywhere rebuilds fresh with the new thresholds) rather
 * than mutating anything in place. A rebuild resets that breaker's call
 * history - an accepted trade-off, equivalent to how a restart would behave.
 *
 * <p>{@link SystemSettingResolver} already caches with a short TTL and evicts
 * immediately on write (see its own Javadoc), so a change made in the dev
 * panel is visible on the very next outbound call, the same way every other
 * dynamic setting in this app already works (see e.g. {@code
 * RequestLoggingFilter}) - no separate polling/scheduling needed here.
 */
@Slf4j
@Component
public class DynamicCircuitBreakerLookup {

    private static final String KEY_PREFIX = "resilience.circuitbreaker.";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final SystemSettingResolver systemSettingResolver;
    private final ConcurrentHashMap<String, AppliedThresholds> applied = new ConcurrentHashMap<>();

    public DynamicCircuitBreakerLookup(
            CircuitBreakerRegistry circuitBreakerRegistry,
            SystemSettingResolver systemSettingResolver
    ) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.systemSettingResolver = systemSettingResolver;
    }

    /** API2 / LIS: one shared instance, instance name equal to its config name. */
    public CircuitBreaker forName(String name) {
        return resolve(name, name);
    }

    /** IAM remote / OAuth2 login / RSA public key: one instance per provider key. */
    public CircuitBreaker forProvider(String configName, String providerKey) {
        return resolve(configName + "-" + providerKey, configName);
    }

    private CircuitBreaker resolve(String instanceName, String configName) {
        refreshIfChanged(configName);
        return circuitBreakerRegistry.circuitBreaker(instanceName, configName);
    }

    private void refreshIfChanged(String configName) {
        CircuitBreakerConfig baseline = circuitBreakerRegistry.getConfiguration(configName)
                .orElseThrow(() -> new IllegalStateException(
                        "No resilience4j circuit breaker config template named '" + configName
                                + "' - check resilience4j.circuitbreaker.configs." + configName
                                + " / instances." + configName + " in application.properties"));

        AppliedThresholds current = applied.computeIfAbsent(configName, key -> AppliedThresholds.from(baseline));
        AppliedThresholds resolved = resolveThresholds(configName, current);

        if (resolved.equals(current)) {
            return;
        }

        try {
            CircuitBreakerConfig updated = CircuitBreakerConfig.from(baseline)
                    .failureRateThreshold(resolved.failureRateThreshold())
                    .slidingWindowSize(resolved.slidingWindowSize())
                    .minimumNumberOfCalls(resolved.minimumNumberOfCalls())
                    .waitDurationInOpenState(Duration.ofSeconds(resolved.waitDurationInOpenStateSeconds()))
                    .slowCallRateThreshold(resolved.slowCallRateThreshold())
                    .slowCallDurationThreshold(Duration.ofSeconds(resolved.slowCallDurationThresholdSeconds()))
                    .permittedNumberOfCallsInHalfOpenState(resolved.permittedNumberOfCallsInHalfOpenState())
                    .automaticTransitionFromOpenToHalfOpenEnabled(resolved.automaticTransitionFromOpenToHalfOpen())
                    .build();

            circuitBreakerRegistry.addConfiguration(configName, updated);
            applied.put(configName, resolved);

            circuitBreakerRegistry.getAllCircuitBreakers().stream()
                    .map(CircuitBreaker::getName)
                    .filter(name -> name.equals(configName) || name.startsWith(configName + "-"))
                    .forEach(circuitBreakerRegistry::remove);

            log.info("event=circuit_breaker_config_updated configName={} failureRateThreshold={} "
                            + "slidingWindowSize={} minimumNumberOfCalls={} waitDurationInOpenStateSeconds={} "
                            + "slowCallRateThreshold={} slowCallDurationThresholdSeconds={} "
                            + "permittedNumberOfCallsInHalfOpenState={} automaticTransitionFromOpenToHalfOpen={}",
                    configName,
                    resolved.failureRateThreshold(),
                    resolved.slidingWindowSize(),
                    resolved.minimumNumberOfCalls(),
                    resolved.waitDurationInOpenStateSeconds(),
                    resolved.slowCallRateThreshold(),
                    resolved.slowCallDurationThresholdSeconds(),
                    resolved.permittedNumberOfCallsInHalfOpenState(),
                    resolved.automaticTransitionFromOpenToHalfOpen());
        } catch (IllegalArgumentException invalidThresholds) {
            log.warn("event=circuit_breaker_config_update_rejected configName={} failureType={} message={} - "
                            + "keeping the previously applied thresholds",
                    configName, invalidThresholds.getClass().getSimpleName(), invalidThresholds.getMessage());
        }
    }

    private AppliedThresholds resolveThresholds(String configName, AppliedThresholds current) {
        return new AppliedThresholds(
                (float) systemSettingResolver.resolveLong(
                        key(configName, "failure-rate-threshold"), (long) current.failureRateThreshold()),
                (int) systemSettingResolver.resolveLong(
                        key(configName, "sliding-window-size"), current.slidingWindowSize()),
                (int) systemSettingResolver.resolveLong(
                        key(configName, "minimum-number-of-calls"), current.minimumNumberOfCalls()),
                systemSettingResolver.resolveLong(
                        key(configName, "wait-duration-in-open-state-seconds"),
                        current.waitDurationInOpenStateSeconds()),
                (float) systemSettingResolver.resolveLong(
                        key(configName, "slow-call-rate-threshold"), (long) current.slowCallRateThreshold()),
                systemSettingResolver.resolveLong(
                        key(configName, "slow-call-duration-threshold-seconds"),
                        current.slowCallDurationThresholdSeconds()),
                (int) systemSettingResolver.resolveLong(
                        key(configName, "permitted-number-of-calls-in-half-open-state"),
                        current.permittedNumberOfCallsInHalfOpenState()),
                systemSettingResolver.resolveBoolean(
                        key(configName, "automatic-transition-from-open-to-half-open-state"),
                        current.automaticTransitionFromOpenToHalfOpen())
        );
    }

    private String key(String configName, String knob) {
        return KEY_PREFIX + configName + "." + knob;
    }

    private record AppliedThresholds(
            float failureRateThreshold,
            int slidingWindowSize,
            int minimumNumberOfCalls,
            long waitDurationInOpenStateSeconds,
            float slowCallRateThreshold,
            long slowCallDurationThresholdSeconds,
            int permittedNumberOfCallsInHalfOpenState,
            boolean automaticTransitionFromOpenToHalfOpen
    ) {
        static AppliedThresholds from(CircuitBreakerConfig config) {
            long waitSeconds = Duration.ofMillis(
                    config.getWaitIntervalFunctionInOpenState().apply(1)).toSeconds();

            return new AppliedThresholds(
                    config.getFailureRateThreshold(),
                    config.getSlidingWindowSize(),
                    config.getMinimumNumberOfCalls(),
                    waitSeconds,
                    config.getSlowCallRateThreshold(),
                    config.getSlowCallDurationThreshold().toSeconds(),
                    config.getPermittedNumberOfCallsInHalfOpenState(),
                    config.isAutomaticTransitionFromOpenToHalfOpenEnabled()
            );
        }
    }
}
