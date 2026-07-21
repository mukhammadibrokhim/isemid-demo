package uz.uzinfocom.app.platform.security.principal;

import java.util.UUID;

/**
 * The authenticated identity for an inbound-integration (machine) caller —
 * a registered {@code IntegrationClient}, bound to a default organization.
 * Distinct from {@link PrincipalUser}: there is no human behind this
 * identity, so it never goes through IAM sync.
 */
public record IntegrationClientPrincipal(
        String clientId,
        String sourceKey,
        Long organizationId,
        UUID organizationUuid
) {
}
