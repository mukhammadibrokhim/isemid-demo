package uz.uzinfocom.app.platform.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationCredentialAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.IntegrationScopeAuthorities;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.time.Duration;
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

    /**
     * {@code lastUsedAt} is an "is this client still alive" indicator, not an audit log -
     * writing it on every request turned each integration read into a DB write.
     */
    private static final Duration LAST_USED_WRITE_INTERVAL = Duration.ofMinutes(5);

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

        Instant now = Instant.now();
        if (client.getLastUsedAt() == null || client.getLastUsedAt().isBefore(now.minus(LAST_USED_WRITE_INTERVAL))) {
            integrationClientRepository.touchLastUsedAt(client.getId(), now, now.minus(LAST_USED_WRITE_INTERVAL));
            client.setLastUsedAt(now);
        }
    }
}
