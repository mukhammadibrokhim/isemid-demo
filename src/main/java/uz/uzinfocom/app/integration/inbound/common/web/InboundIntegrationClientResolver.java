package uz.uzinfocom.app.integration.inbound.common.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;

/**
 * Resolves the numeric {@link IntegrationClient} id behind the current
 * inbound-submission caller, so it can be stored on the created
 * Form058/Form0581 row as {@code sourceIntegrationClientId} - closing the gap
 * between {@link IntegrationClientAuthentication}'s business-string
 * {@code clientId} (all it carries) and the PK the outbound webhook feature
 * needs to look the client back up. Returns {@code null} for an SSO/DHP
 * caller (see {@link InboundCallerContext}, same non-integration-client
 * distinction) - those submissions have no webhook to notify back.
 */
@Component
public class InboundIntegrationClientResolver {

    private final IntegrationClientRepository integrationClientRepository;

    public InboundIntegrationClientResolver(IntegrationClientRepository integrationClientRepository) {
        this.integrationClientRepository = integrationClientRepository;
    }

    public Long resolveSourceIntegrationClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof IntegrationClientAuthentication integrationAuthentication)) {
            return null;
        }

        String clientId = integrationAuthentication.getPrincipal().clientId();
        return integrationClientRepository.findByClientId(clientId)
                .map(IntegrationClient::getId)
                .orElse(null);
    }
}
