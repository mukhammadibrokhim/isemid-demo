package uz.uzinfocom.app.platform.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.springboot.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import uz.uzinfocom.app.integration.api2.common.exception.Api2BadRequestException;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpAccessDeniedException;
import uz.uzinfocom.app.integration.lis.common.exception.LisBadRequestException;
import uz.uzinfocom.app.platform.ssoproxy.application.exception.InvalidLoginCredentialsException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * IntelliJ flags {@code configs.*.ignore-exceptions} as unresolved (it can't
 * resolve the {@code Class<? extends Throwable>[]} element type through the
 * {@code configs} map), and silently-ignored typos in this block have bitten
 * before ({@code automatic-transition-...-state} instead of {@code -enabled}).
 * Binds the real {@code application.properties} exactly like Spring Boot does
 * and builds the resulting {@link CircuitBreakerConfig}, so a key that doesn't
 * actually bind fails here instead of being dropped at runtime.
 */
class CircuitBreakerPropertiesBindingTest {

    private static CircuitBreakerProperties properties;

    @BeforeAll
    static void bind() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new PropertiesPropertySource("application",
                PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"))));
        properties = new Binder(ConfigurationPropertySources.get(environment))
                .bind("resilience4j.circuitbreaker", Bindable.of(CircuitBreakerProperties.class))
                .orElseThrow(() -> new AssertionError("resilience4j.circuitbreaker did not bind"));
    }

    @Test
    void defaultConfigAutomaticallyTransitionsToHalfOpen() {
        assertThat(build("default").isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("ignoredExceptions")
    void ignoreExceptionsBindAndAreIgnoredByTheBuiltConfig(String config, Class<? extends Throwable> ignored) {
        assertThat(properties.getConfigs().get(config).getIgnoreExceptions()).contains(ignored);

        CircuitBreakerConfig built = build(config);
        assertThat(built.getIgnoreExceptionPredicate().test(mock(ignored))).isTrue();
        assertThat(built.getIgnoreExceptionPredicate().test(new IllegalStateException())).isFalse();
        // base-config=default is inherited, not replaced
        assertThat(built.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
    }

    private static Stream<Arguments> ignoredExceptions() {
        return Stream.of(
                Arguments.of("api2", Api2BadRequestException.class),
                Arguments.of("lis", LisBadRequestException.class),
                Arguments.of("dhp", DhpAccessDeniedException.class),
                Arguments.of("oauth2-login", InvalidLoginCredentialsException.class));
    }

    private static CircuitBreakerConfig build(String config) {
        return properties.createCircuitBreakerConfig(config, properties.getConfigs().get(config),
                new CompositeCustomizer<>(List.of()));
    }
}
