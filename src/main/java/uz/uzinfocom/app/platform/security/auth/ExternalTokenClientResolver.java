package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Matches an SSO/DHP M2M ({@code client_credentials}) token to a registered
 * {@link IntegrationAuthType#EXTERNAL_TOKEN} client. Providers name the
 * client claim differently ({@code client_id}, {@code azp} or {@code sub}),
 * so each is tried in that order; the first one that matches an active
 * registered client wins. A human token never matches: its {@code sub} is a
 * user id, which no client is registered under.
 */
@Component
@RequiredArgsConstructor
public class ExternalTokenClientResolver {

    private static final List<String> CLIENT_CLAIMS = List.of("client_id", "azp", "sub");

    private final IntegrationClientRepository integrationClientRepository;

    public Optional<IntegrationClient> resolve(String providerKey, Map<String, Object> claims) {
        for (String claimName : CLIENT_CLAIMS) {
            Object claim = claims.get(claimName);
            if (!(claim instanceof String externalClientId) || !StringUtils.hasText(externalClientId)) {
                continue;
            }

            Optional<IntegrationClient> client = integrationClientRepository
                    .findByProviderKeyAndExternalClientIdAndAuthTypeAndActiveTrue(
                            providerKey, externalClientId, IntegrationAuthType.EXTERNAL_TOKEN);
            if (client.isPresent()) {
                return client;
            }
        }

        return Optional.empty();
    }
}
