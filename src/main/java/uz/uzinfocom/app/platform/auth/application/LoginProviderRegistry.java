package uz.uzinfocom.app.platform.auth.application;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.auth.application.exception.UnknownLoginProviderException;
import uz.uzinfocom.app.platform.auth.properties.LoginGrantType;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds one {@link LoginProvider} per configured provider key, covering
 * every OAuth2 grant this module knows about - the login-proxy counterpart
 * of {@code ProviderAuthenticationManagerRegistry} on the inbound side.
 *
 * <p>Each {@code app.auth.login.providers.<key>.*} entry declares its
 * {@code grantType} (only {@code AUTHORIZATION_CODE} exists today, e.g.
 * "sso-web", "dhp-web"); this registry dispatches it to the
 * matching {@link LoginProviderFactory} - collected via {@code
 * List<LoginProviderFactory>} injection, one {@code @Component} per grant
 * type, exactly like {@code IdentityClaimExtractorRegistry} collects {@code
 * List<IdentityClaimExtractor>}. Adding a provider that speaks an
 * already-supported grant is a config-only change; adding a provider that
 * needs a new grant type means adding a {@link LoginGrantType} value and a
 * matching {@link LoginProviderFactory} bean - this class itself never
 * changes either way.</p>
 *
 * <p>A hand-written {@code @Component implements LoginProvider} (collected
 * via {@code List<LoginProvider>}) takes priority over a configured entry
 * with the same key, for a provider whose flow doesn't fit any factory at
 * all.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(LoginProvidersProperties.class)
public class LoginProviderRegistry {

    private final LoginProvidersProperties properties;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final List<LoginProvider> customProviders;
    private final List<LoginProviderFactory> factories;

    private Map<String, LoginProvider> providersByKey = Map.of();

    @PostConstruct
    void initialize() {
        Map<LoginGrantType, LoginProviderFactory> factoriesByGrantType = factories.stream()
                .collect(Collectors.toMap(LoginProviderFactory::supportedGrantType, Function.identity()));

        LinkedHashMap<String, LoginProvider> registered = new LinkedHashMap<>();

        for (LoginProvider customProvider : customProviders) {
            registered.put(customProvider.providerKey(), customProvider);
            log.info("Login provider registered. providerKey={}, kind=custom", customProvider.providerKey());
        }

        properties.getProviders().forEach((providerKey, providerProperties) -> {
            if (providerProperties == null || !providerProperties.isEnabled()) {
                log.info("Login provider skipped. providerKey={}, reason=disabled", providerKey);
                return;
            }

            if (registered.containsKey(providerKey)) {
                log.info("Login provider skipped. providerKey={}, reason=custom bean already registered", providerKey);
                return;
            }

            LoginGrantType grantType = providerProperties.getGrantType();
            LoginProviderFactory factory = factoriesByGrantType.get(grantType);

            if (factory == null) {
                log.warn("Login provider skipped. providerKey={}, reason=no factory registered for grantType={}",
                        providerKey, grantType);
                return;
            }

            registered.put(providerKey, factory.create(providerKey, providerProperties, restClient, jsonMapper));

            log.info("Login provider registered. providerKey={}, grantType={}, tokenUrl={}",
                    providerKey, grantType, providerProperties.getTokenUrl());
        });

        this.providersByKey = Map.copyOf(registered);
    }

    public LoginProvider resolve(String providerKey) {
        LoginProvider provider = providersByKey.get(providerKey);

        if (provider == null) {
            throw new UnknownLoginProviderException(providerKey);
        }

        return provider;
    }
}
