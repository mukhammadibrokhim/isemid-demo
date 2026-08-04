package uz.uzinfocom.app.platform.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationCredentialAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.IntegrationScopeAuthorities;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.time.Instant;
import java.util.UUID;

/**
 * Shared final step for every non-JWT integration-client credential filter
 * (API key, Basic Auth, IP allow-list): once a filter has resolved and
 * verified a specific {@link IntegrationClient}, this builds the same
 * {@code SCOPE_*}-bearing principal the client-credentials JWT flow builds
 * (see {@code IntegrationJwtAuthenticationConverter}) and installs it.
 */
@Component
@RequiredArgsConstructor
public class IntegrationCredentialAuthenticator {

    private final OrganizationRepository organizationRepository;
    private final IntegrationClientRepository integrationClientRepository;

    public void authenticate(IntegrationClient client) {
        UUID organizationUuid = organizationRepository.findById(client.getOrganizationId())
                .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"))
                .getUuid();

        IntegrationClientPrincipal principal = new IntegrationClientPrincipal(
                client.getClientId(), client.getSourceKey(), client.getOrganizationId(), organizationUuid);

        SecurityContextHolder.getContext().setAuthentication(new IntegrationCredentialAuthenticationToken(
                client.getAuthType(), principal, IntegrationScopeAuthorities.from(client.getScopes())));

        client.setLastUsedAt(Instant.now());
        integrationClientRepository.save(client);
    }
}
