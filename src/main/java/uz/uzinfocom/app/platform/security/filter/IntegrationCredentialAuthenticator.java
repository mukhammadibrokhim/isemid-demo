package uz.uzinfocom.app.platform.security.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    /**
     * An organization's UUID never changes, and {@code OrganizationContextFilter} already
     * re-loads the full organization on every request - this lookup only needs the UUID
     * for the principal, so it doesn't have to hit the DB a second time per request.
     */
    private final Cache<Long, UUID> organizationUuidById = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(10_000)
            .build();

    /**
     * True when a real (non-anonymous) authentication is already installed. Spring's
     * anonymous filter runs earlier in the chain, so a plain {@code getAuthentication() != null}
     * is always true and would make every credential filter skip itself.
     */
    public static boolean isAlreadyAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /** Principal for a resolved client, also used by the M2M-token converter. */
    public IntegrationClientPrincipal principalOf(IntegrationClient client) {
        UUID organizationUuid = client.getOrganizationId() == null
                ? null
                : organizationUuidById.get(client.getOrganizationId(), organizationId ->
                organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"))
                        .getUuid());

        return new IntegrationClientPrincipal(
                client.getClientId(), client.getSourceKey(), client.getOrganizationId(), organizationUuid);
    }

    public void authenticate(IntegrationClient client) {
        IntegrationClientPrincipal principal = principalOf(client);

        SecurityContextHolder.getContext().setAuthentication(new IntegrationCredentialAuthenticationToken(
                client.getAuthType(), principal, IntegrationScopeAuthorities.from(client.getScopes())));

        Instant now = Instant.now();
        if (client.getLastUsedAt() == null || client.getLastUsedAt().isBefore(now.minus(LAST_USED_WRITE_INTERVAL))) {
            integrationClientRepository.touchLastUsedAt(client.getId(), now, now.minus(LAST_USED_WRITE_INTERVAL));
            client.setLastUsedAt(now);
        }
    }
}
