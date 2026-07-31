package uz.uzinfocom.app.platform.auth.application;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.auth.application.exception.UnknownLoginProviderException;
import uz.uzinfocom.app.platform.auth.properties.LoginGrantType;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties.ProviderProperties;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginProviderRegistryTest {

    private final RestClient restClient = RestClient.create();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final List<LoginProviderFactory> factories =
            List.of(new AuthorizationCodeGrantLoginProviderFactory(
                    TestCircuitBreakerLookups.withDefaults(CircuitBreakerRegistry.ofDefaults())));

    // No DB override in these tests - always fall through to the caller-supplied default,
    // exactly as if the system_settings table were empty.
    private final SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);

    {
        when(systemSettingResolver.resolveBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    private ProviderProperties provider(boolean enabled, String tokenUrl) {
        ProviderProperties properties = new ProviderProperties();
        properties.setEnabled(enabled);
        properties.setTokenUrl(tokenUrl);
        properties.setClientId("client");
        properties.setClientSecret("secret");
        return properties;
    }

    private LoginProviderRegistry registry(LoginProvidersProperties properties, List<LoginProvider> customProviders) {
        return registry(properties, customProviders, factories);
    }

    private LoginProviderRegistry registry(
            LoginProvidersProperties properties,
            List<LoginProvider> customProviders,
            List<LoginProviderFactory> providerFactories
    ) {
        LoginProviderRegistry registry = new LoginProviderRegistry(
                properties, restClient, jsonMapper, customProviders, providerFactories, systemSettingResolver);
        registry.initialize();
        return registry;
    }

    @Test
    void registersEveryEnabledConfiguredProvider() {
        LoginProvidersProperties properties = new LoginProvidersProperties();
        Map<String, ProviderProperties> providers = new LinkedHashMap<>();
        providers.put("sso-web", provider(true, "https://sso.example/oauth/token"));
        providers.put("dhp-web", provider(true, "https://dhp.example/oauth/token"));
        providers.put("disabled-one", provider(false, "https://disabled.example/oauth/token"));
        properties.setProviders(providers);

        LoginProviderRegistry registry = registry(properties, List.of());

        assertThat(registry.resolve("sso-web").providerKey()).isEqualTo("sso-web");
        assertThat(registry.resolve("sso-web").grantType()).isEqualTo(LoginGrantType.AUTHORIZATION_CODE);
        assertThat(registry.resolve("dhp-web").providerKey()).isEqualTo("dhp-web");
        assertThat(registry.resolve("dhp-web").grantType()).isEqualTo(LoginGrantType.AUTHORIZATION_CODE);
        assertThatThrownBy(() -> registry.resolve("disabled-one"))
                .isInstanceOf(UnknownLoginProviderException.class);
    }

    @Test
    void resolvingAnUnconfiguredProviderThrows() {
        LoginProviderRegistry registry = registry(new LoginProvidersProperties(), List.of());

        assertThatThrownBy(() -> registry.resolve("unknown"))
                .isInstanceOf(UnknownLoginProviderException.class);
    }

    @Test
    void skipsAnEntryWhoseGrantTypeHasNoRegisteredFactory() {
        LoginProvidersProperties properties = new LoginProvidersProperties();
        Map<String, ProviderProperties> providers = new LinkedHashMap<>();
        providers.put("dhp-web", provider(true, "https://dhp.example/oauth/token"));
        properties.setProviders(providers);

        LoginProviderRegistry registry = registry(properties, List.of(), List.of());

        assertThatThrownBy(() -> registry.resolve("dhp-web"))
                .isInstanceOf(UnknownLoginProviderException.class);
    }

    @Test
    void aHandWrittenLoginProviderBeanTakesPriorityOverAConfiguredEntryWithTheSameKey() {
        LoginProvidersProperties properties = new LoginProvidersProperties();
        Map<String, ProviderProperties> providers = new LinkedHashMap<>();
        providers.put("dhp-web", provider(true, "https://dhp.example/oauth/token"));
        properties.setProviders(providers);

        LoginProvider customDhpProvider = new LoginProvider() {
            @Override
            public String providerKey() {
                return "dhp-web";
            }

            @Override
            public LoginGrantType grantType() {
                return LoginGrantType.AUTHORIZATION_CODE;
            }

            @Override
            public LoginResult login(uz.uzinfocom.app.platform.auth.web.dto.LoginRequest request) {
                return new LoginResult("custom-token", null, "Bearer", null, null);
            }

            @Override
            public LoginResult refresh(String refreshToken) {
                return new LoginResult("refreshed-custom-token", null, "Bearer", null, null);
            }

            @Override
            public void logout(String accessToken, String refreshToken) {
                // no-op for this test double
            }
        };

        LoginProviderRegistry registry = registry(properties, List.of(customDhpProvider));

        assertThat(registry.resolve("dhp-web")).isSameAs(customDhpProvider);
    }
}
