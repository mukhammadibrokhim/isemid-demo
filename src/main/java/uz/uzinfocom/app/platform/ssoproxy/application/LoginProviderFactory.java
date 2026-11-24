package uz.uzinfocom.app.platform.ssoproxy.application;

import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginGrantType;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginProvidersProperties.ProviderProperties;

/**
 * Builds the config-driven {@link LoginProvider} for one {@link
 * LoginGrantType}. {@link LoginProviderRegistry} collects every {@code
 * @Component implements LoginProviderFactory} via {@code
 * List<LoginProviderFactory>} injection and dispatches each configured
 * provider entry to the factory matching its {@code grantType} - the same
 * "collect implementations by key" pattern {@code
 * IdentityClaimExtractorRegistry} uses on the inbound-JWT side.
 *
 * <p>This is the actual extension point for a grant this module doesn't
 * speak yet: add a {@link LoginGrantType} value and a new {@code @Component
 * implements LoginProviderFactory} for it - {@link LoginProviderRegistry}
 * itself never needs to change.</p>
 */
public interface LoginProviderFactory {

    LoginGrantType supportedGrantType();

    LoginProvider create(String providerKey, ProviderProperties properties, RestClient restClient, JsonMapper jsonMapper);
}
