package uz.uzinfocom.app.platform.ssoproxy.application;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.ssoproxy.application.exception.UnknownLoginProviderException;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginGrantType;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginProvidersProperties;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

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
    private final SystemSettingResolver systemSettingResolver;

    private Map<String, LoginProvider> providersByKey = Map.of();

    /**
     * Only configured (factory-built) providers get an entry here - their
     * enabled/disabled state can be overridden at runtime via
     * {@code app.auth.login.providers.<key>.enabled} (see {@link #resolve}).
     * Hand-written custom providers are never gated this way - there was
     * never a per-provider enabled toggle for them, so their absence here
     * means "always resolvable."
     */
    private Map<String, Boolean> defaultEnabledByProviderKey = Map.of();

    @PostConstruct
    void initialize() {
        Map<LoginGrantType, LoginProviderFactory> factoriesByGrantType = factories.stream()
                .collect(Collectors.toMap(LoginProviderFactory::supportedGrantType, Function.identity()));

        LinkedHashMap<String, LoginProvider> registered = new LinkedHashMap<>();
        LinkedHashMap<String, Boolean> defaultEnabled = new LinkedHashMap<>();

        for (LoginProvider customProvider : customProviders) {
            registered.put(customProvider.providerKey(), customProvider);
            log.info("Login provider registered. providerKey={}, kind=custom", customProvider.providerKey());
        }

        // Registered regardless of providerProperties.isEnabled() - the enabled check now
        // happens dynamically in resolve() so it can be overridden at runtime without a
        // restart (see SystemSettingResolver). Only a genuinely unconfigured/unfactorable
        // entry is skipped here, at startup.
        properties.getProviders().forEach((providerKey, providerProperties) -> {
            if (providerProperties == null) {
                log.info("Login provider skipped. providerKey={}, reason=no configuration", providerKey);
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
            defaultEnabled.put(providerKey, providerProperties.isEnabled());

            log.info("Login provider registered. providerKey={}, grantType={}, tokenUrl={}, enabledByDefault={}",
                    providerKey, grantType, providerProperties.getTokenUrl(), providerProperties.isEnabled());
        });

        this.providersByKey = Map.copyOf(registered);
        this.defaultEnabledByProviderKey = Map.copyOf(defaultEnabled);
    }

    public LoginProvider resolve(String providerKey) {
        LoginProvider provider = providersByKey.get(providerKey);

        if (provider == null) {
            throw new UnknownLoginProviderException(providerKey);
        }

        Boolean defaultEnabled = defaultEnabledByProviderKey.get(providerKey);
        if (defaultEnabled != null) {
            boolean enabled = systemSettingResolver.resolveBoolean(
                    "app.auth.login.providers." + providerKey + ".enabled", defaultEnabled);
            if (!enabled) {
                throw new UnknownLoginProviderException(providerKey);
            }
        }

        return provider;
    }
}
