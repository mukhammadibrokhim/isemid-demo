package uz.uzinfocom.app.platform.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Re-resolves connect/read timeout and connection-pool sizing from {@link
 * SystemSettingResolver} on every outbound request and, only when a value
 * has actually drifted, applies it live to the owning {@link
 * PoolingHttpClientConnectionManager} / {@link
 * HttpComponentsClientHttpRequestFactory} - editable at runtime from the dev
 * panel ({@code DevSettingController}, {@code /v1/dev/**}) under keys
 * {@code http-client.<name>.<knob>}, the same mechanism {@code
 * DynamicCircuitBreakerLookup} already uses for circuit breaker thresholds.
 *
 * <p>Unlike a circuit breaker, there is nothing here to short-circuit -
 * this interceptor never blocks or rejects a request, it only keeps the
 * shared {@link PoolingHttpClientConnectionManager}/{@link
 * HttpComponentsClientHttpRequestFactory} in sync with the latest settings
 * before letting the request through.
 *
 * <p>{@code connectionRequestTimeoutMillis}/{@code timeToLiveSeconds} are
 * {@code null} for API2/LIS - those two only exist as explicit, tunable
 * concepts on the general-purpose platform HTTP client today (see {@code
 * PlatformRestClientProperties}); this class simply skips resolving/applying
 * whichever of the two isn't applicable rather than inventing a default for
 * an integration that never had one.
 *
 * <p>{@code setDefaultConnectionConfig} (connect-timeout, socket-timeout,
 * time-to-live) only affects <em>new</em> pooled connections going forward -
 * a connection already leased out keeps whatever config it was created with
 * until it's evicted - the same "eventually applied, not instant for
 * everything in flight" trade-off {@code DynamicCircuitBreakerLookup}
 * accepts for a breaker's call-history reset.
 */
@Slf4j
public class DynamicHttpTuningInterceptor implements ClientHttpRequestInterceptor {

    private static final String KEY_PREFIX = "http-client.";

    private final String name;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final HttpComponentsClientHttpRequestFactory requestFactory;
    private final SystemSettingResolver systemSettingResolver;
    private final AtomicReference<AppliedTuning> applied;

    public DynamicHttpTuningInterceptor(
            String name,
            PoolingHttpClientConnectionManager connectionManager,
            HttpComponentsClientHttpRequestFactory requestFactory,
            SystemSettingResolver systemSettingResolver,
            long initialConnectTimeoutMillis,
            long initialReadTimeoutMillis,
            Long initialConnectionRequestTimeoutMillis,
            Long initialTimeToLiveSeconds
    ) {
        this.name = name;
        this.connectionManager = connectionManager;
        this.requestFactory = requestFactory;
        this.systemSettingResolver = systemSettingResolver;
        this.applied = new AtomicReference<>(new AppliedTuning(
                initialConnectTimeoutMillis,
                initialReadTimeoutMillis,
                initialConnectionRequestTimeoutMillis,
                initialTimeToLiveSeconds,
                connectionManager.getMaxTotal(),
                connectionManager.getDefaultMaxPerRoute()
        ));
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        refreshIfChanged();
        return execution.execute(request, body);
    }

    private void refreshIfChanged() {
        AppliedTuning current = applied.get();
        AppliedTuning resolved = resolve(current);

        if (resolved.equals(current)) {
            return;
        }

        try {
            applyToClient(resolved);
            applied.set(resolved);
            log.info("event=http_client_tuning_updated name={} connectTimeoutMillis={} readTimeoutMillis={} "
                            + "connectionRequestTimeoutMillis={} timeToLiveSeconds={} maxConnections={} "
                            + "maxConnectionsPerRoute={}",
                    name,
                    resolved.connectTimeoutMillis(),
                    resolved.readTimeoutMillis(),
                    resolved.connectionRequestTimeoutMillis(),
                    resolved.timeToLiveSeconds(),
                    resolved.maxConnections(),
                    resolved.maxConnectionsPerRoute());
        } catch (IllegalArgumentException invalidTuning) {
            log.warn("event=http_client_tuning_update_rejected name={} failureType={} message={} - "
                            + "keeping the previously applied values",
                    name, invalidTuning.getClass().getSimpleName(), invalidTuning.getMessage());
        }
    }

    private AppliedTuning resolve(AppliedTuning current) {
        long connectTimeoutMillis = systemSettingResolver.resolveLong(
                key("connect-timeout-millis"), current.connectTimeoutMillis());
        long readTimeoutMillis = systemSettingResolver.resolveLong(
                key("read-timeout-millis"), current.readTimeoutMillis());
        Long connectionRequestTimeoutMillis = current.connectionRequestTimeoutMillis() == null
                ? null
                : systemSettingResolver.resolveLong(
                        key("connection-request-timeout-millis"), current.connectionRequestTimeoutMillis());
        Long timeToLiveSeconds = current.timeToLiveSeconds() == null
                ? null
                : systemSettingResolver.resolveLong(
                        key("connection-time-to-live-seconds"), current.timeToLiveSeconds());
        int maxConnections = (int) systemSettingResolver.resolveLong(
                key("max-connections"), current.maxConnections());
        int maxConnectionsPerRoute = (int) systemSettingResolver.resolveLong(
                key("max-connections-per-route"), current.maxConnectionsPerRoute());

        return new AppliedTuning(
                connectTimeoutMillis,
                readTimeoutMillis,
                connectionRequestTimeoutMillis,
                timeToLiveSeconds,
                maxConnections,
                maxConnectionsPerRoute
        );
    }

    private void applyToClient(AppliedTuning tuning) {
        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(tuning.connectTimeoutMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(tuning.readTimeoutMillis()));

        if (tuning.timeToLiveSeconds() != null) {
            connectionConfigBuilder.setTimeToLive(TimeValue.ofSeconds(tuning.timeToLiveSeconds()));
        }

        connectionManager.setDefaultConnectionConfig(connectionConfigBuilder.build());
        connectionManager.setMaxTotal(tuning.maxConnections());
        connectionManager.setDefaultMaxPerRoute(tuning.maxConnectionsPerRoute());

        requestFactory.setReadTimeout(Duration.ofMillis(tuning.readTimeoutMillis()));
        if (tuning.connectionRequestTimeoutMillis() != null) {
            requestFactory.setConnectionRequestTimeout(Duration.ofMillis(tuning.connectionRequestTimeoutMillis()));
        }
    }

    private String key(String knob) {
        return KEY_PREFIX + name + "." + knob;
    }

    private record AppliedTuning(
            long connectTimeoutMillis,
            long readTimeoutMillis,
            Long connectionRequestTimeoutMillis,
            Long timeToLiveSeconds,
            int maxConnections,
            int maxConnectionsPerRoute
    ) {
    }
}
