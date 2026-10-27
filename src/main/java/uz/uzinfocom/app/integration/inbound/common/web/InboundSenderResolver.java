package uz.uzinfocom.app.integration.inbound.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.UUID;

/**
 * Resolves which organization an inbound submission is made for.
 * <ul>
 *   <li>Integration client (any auth type): the organization is named in the body
 *   ({@code senderOrganizationUuid}) and may be any active organization (an EXTERNAL_TOKEN
 *   client only one of its provider). The submitter block is required too, so the audit
 *   records who sent the form and for which organization.</li>
 *   <li>Every other caller keeps the selected organization (SSO/DHP users); a body value,
 *   if given, must match it.</li>
 * </ul>
 * The resolved organization is installed in {@link CurrentOrganizationContext} so the
 * downstream create validators see it exactly as in the header-driven flow.
 */
@Component
@RequiredArgsConstructor
public class InboundSenderResolver {

    private final IntegrationClientRepository integrationClientRepository;
    private final OrganizationRepository organizationRepository;

    public Long resolveSenderOrganizationId(UUID bodyOrganizationUuid, InboundSubmitterRequest submitter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof IntegrationClientAuthentication integrationAuthentication) {
            IntegrationClient client = integrationClientRepository
                    .findByClientId(integrationAuthentication.getPrincipal().clientId())
                    .orElseThrow(() -> new AccessDeniedException("integration.client.inactive"));

            // Integration clients carry no organization of their own: the sending organization
            // and the submitting person are always named in the body.
            return resolveFromBody(client, bodyOrganizationUuid, submitter);
        }

        if (bodyOrganizationUuid != null
                && !bodyOrganizationUuid.equals(CurrentOrganizationContext.getRequiredOrganizationUuid())) {
            throw new AccessDeniedException("organization.not_allowed");
        }

        return InboundCallerContext.resolveSenderOrganizationId();
    }

    private Long resolveFromBody(
            IntegrationClient client,
            UUID bodyOrganizationUuid,
            InboundSubmitterRequest submitter
    ) {
        if (bodyOrganizationUuid == null) {
            throw new InboundValidationException("integration.sender-organization.required");
        }
        if (submitter == null) {
            throw new InboundValidationException("integration.submitter.required");
        }

        Organization organization = organizationRepository.findByUuid(bodyOrganizationUuid)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActive()))
                .orElseThrow(() -> new InboundValidationException("integration.sender-organization.not-found"));

        // Only EXTERNAL_TOKEN clients belong to a provider; the provider is their only boundary.
        boolean providerMatches = client.getProviderKey() == null
                || client.getProviderKey().equals(organization.getProviderKey());

        if (!providerMatches) {
            throw new AccessDeniedException("organization.not_allowed");
        }

        CurrentOrganizationContext.set(organization);
        return organization.getId();
    }
}
