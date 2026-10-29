package uz.uzinfocom.app.platform.ssoproxy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginGrantType;
import uz.uzinfocom.app.platform.ssoproxy.properties.LoginProvidersProperties.ProviderProperties;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

@Component
@RequiredArgsConstructor
public class AuthorizationCodeGrantLoginProviderFactory implements LoginProviderFactory {

    private final DynamicCircuitBreakerLookup circuitBreakerLookup;

    @Override
    public LoginGrantType supportedGrantType() {
        return LoginGrantType.AUTHORIZATION_CODE;
    }

    @Override
    public LoginProvider create(
            String providerKey,
            ProviderProperties properties,
            RestClient restClient,
            JsonMapper jsonMapper
    ) {
        return new AuthorizationCodeGrantLoginProvider(
                providerKey, properties, restClient, jsonMapper, circuitBreakerLookup);
    }
}
