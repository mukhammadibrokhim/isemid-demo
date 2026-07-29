package uz.uzinfocom.app.platform.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A {@link DynamicCircuitBreakerLookup} wired to a mocked {@link
 * SystemSettingResolver} that always echoes back the caller's default -
 * i.e. behaves exactly like an empty {@code system_settings} table, so
 * tests get the same static thresholds the registry was built with.
 */
public final class TestCircuitBreakerLookups {

    private TestCircuitBreakerLookups() {
    }

    /**
     * Registers each name as a named config template with resilience4j's
     * plain defaults - mirroring what Spring Boot's auto-configuration does
     * from {@code resilience4j.circuitbreaker.configs.*}/{@code instances.*}
     * in production - since {@link DynamicCircuitBreakerLookup} requires
     * {@code registry.getConfiguration(configName)} to already resolve.
     */
    public static DynamicCircuitBreakerLookup withDefaults(String... configNames) {
        return withDefaults(CircuitBreakerRegistry.of(Arrays.stream(configNames)
                .collect(Collectors.toMap(name -> name, name -> CircuitBreakerConfig.ofDefaults()))));
    }

    public static DynamicCircuitBreakerLookup withDefaults(CircuitBreakerRegistry registry) {
        SystemSettingResolver resolver = mock(SystemSettingResolver.class);
        when(resolver.resolveLong(anyString(), anyLong())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resolver.resolveBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> invocation.getArgument(1));
        return new DynamicCircuitBreakerLookup(registry, resolver);
    }
}
