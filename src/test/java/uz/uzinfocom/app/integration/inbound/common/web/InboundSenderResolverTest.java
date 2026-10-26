package uz.uzinfocom.app.integration.inbound.common.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.IntegrationCredentialAuthenticationToken;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InboundSenderResolverTest {

    private static final String CLIENT_ID = "ic_test";
    private static final long DEFAULT_ORG_ID = 1L;
    private static final long ALLOWED_ORG_ID = 2L;
    private static final long OTHER_ORG_ID = 3L;

    private final IntegrationClientRepository clients = mock(IntegrationClientRepository.class);
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private final InboundSenderResolver resolver = new InboundSenderResolver(clients, organizations);

    private final InboundSubmitterRequest submitter =
            new InboundSubmitterRequest("12345678901234", "Test User", "Doctor", null);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
        CurrentOrganizationContext.clear();
    }

    @Test
    void externalTokenClientMayUseAnyOrganizationOfItsProviderNamedInTheBody() {
        IntegrationClient client = externalTokenClient();
        authenticate(client);
        Organization organization = organization(ALLOWED_ORG_ID, "sso");
        when(organizations.findByUuid(organization.getUuid())).thenReturn(Optional.of(organization));

        Long resolved = resolver.resolveSenderOrganizationId(organization.getUuid(), submitter);

        assertThat(resolved).isEqualTo(ALLOWED_ORG_ID);
        assertThat(CurrentOrganizationContext.require().getId()).isEqualTo(ALLOWED_ORG_ID);
    }

    @Test
    void externalTokenClientMayAlwaysUseItsDefaultOrganization() {
        IntegrationClient client = externalTokenClient();
        authenticate(client);
        Organization organization = organization(DEFAULT_ORG_ID, "sso");
        when(organizations.findByUuid(organization.getUuid())).thenReturn(Optional.of(organization));

        assertThat(resolver.resolveSenderOrganizationId(organization.getUuid(), submitter)).isEqualTo(DEFAULT_ORG_ID);
    }

    @Test
    void externalTokenClientCannotUseAnOrganizationOfAnotherProvider() {
        IntegrationClient client = externalTokenClient();
        authenticate(client);
        Organization organization = organization(ALLOWED_ORG_ID, "dhp");
        when(organizations.findByUuid(organization.getUuid())).thenReturn(Optional.of(organization));

        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(organization.getUuid(), submitter))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void externalTokenClientMustNameTheOrganizationAndTheSubmitter() {
        authenticate(externalTokenClient());

        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(null, submitter))
                .isInstanceOf(InboundValidationException.class);
        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(UUID.randomUUID(), null))
                .isInstanceOf(InboundValidationException.class);
    }

    @Test
    void otherClientTypesKeepTheirBoundOrganizationAndRejectAMismatchingBodyValue() {
        IntegrationClient client = externalTokenClient();
        client.setAuthType(IntegrationAuthType.BASIC_AUTH);
        authenticate(client);
        Organization bound = organization(DEFAULT_ORG_ID, "sso");
        CurrentOrganizationContext.set(bound);

        assertThat(resolver.resolveSenderOrganizationId(null, null)).isEqualTo(DEFAULT_ORG_ID);
        assertThat(resolver.resolveSenderOrganizationId(bound.getUuid(), null)).isEqualTo(DEFAULT_ORG_ID);
        // naming another organization goes through the body path, which also needs the submitter
        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(UUID.randomUUID(), null))
                .isInstanceOf(InboundValidationException.class);
        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(UUID.randomUUID(), submitter))
                .isInstanceOf(InboundValidationException.class);
    }

    @Test
    void clientWithoutDefaultOrganizationMustNameOrganizationAndSubmitterInTheBody() {
        IntegrationClient client = externalTokenClient();
        client.setAuthType(IntegrationAuthType.API_KEY);
        client.setProviderKey(null);
        client.setOrganizationId(null);
        authenticate(client);
        Organization organization = organization(ALLOWED_ORG_ID, "sso");
        when(organizations.findByUuid(organization.getUuid())).thenReturn(Optional.of(organization));

        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(null, submitter))
                .isInstanceOf(InboundValidationException.class);
        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(organization.getUuid(), null))
                .isInstanceOf(InboundValidationException.class);
        // no organization check at all for a client that belongs to no provider
        assertThat(resolver.resolveSenderOrganizationId(organization.getUuid(), submitter)).isEqualTo(ALLOWED_ORG_ID);
    }

    @Test
    void externalTokenClientWithoutDefaultOrganizationMayUseAnyOrganizationOfItsProvider() {
        IntegrationClient client = externalTokenClient();
        client.setOrganizationId(null);
        authenticate(client);
        Organization sameProvider = organization(OTHER_ORG_ID, "sso");
        Organization otherProvider = organization(ALLOWED_ORG_ID, "dhp");
        when(organizations.findByUuid(sameProvider.getUuid())).thenReturn(Optional.of(sameProvider));
        when(organizations.findByUuid(otherProvider.getUuid())).thenReturn(Optional.of(otherProvider));

        assertThat(resolver.resolveSenderOrganizationId(sameProvider.getUuid(), submitter)).isEqualTo(OTHER_ORG_ID);
        assertThatThrownBy(() -> resolver.resolveSenderOrganizationId(otherProvider.getUuid(), submitter))
                .isInstanceOf(AccessDeniedException.class);
    }

    private IntegrationClient externalTokenClient() {
        IntegrationClient client = IntegrationClient.builder()
                .clientId(CLIENT_ID)
                .authType(IntegrationAuthType.EXTERNAL_TOKEN)
                .providerKey("sso")
                .externalClientId("m2m-1")
                .organizationId(DEFAULT_ORG_ID)
                .sourceKey("dmed")
                .scopes("form058:submit")
                .build();
        client.setId(10L);
        when(clients.findByClientId(CLIENT_ID)).thenReturn(Optional.of(client));
        return client;
    }

    private void authenticate(IntegrationClient client) {
        SecurityContextHolder.getContext().setAuthentication(new IntegrationCredentialAuthenticationToken(
                client.getAuthType(),
                new IntegrationClientPrincipal(client.getClientId(), client.getSourceKey(), DEFAULT_ORG_ID, UUID.randomUUID()),
                List.of()));
    }

    private Organization organization(long id, String providerKey) {
        Organization organization = mock(Organization.class);
        when(organization.getId()).thenReturn(id);
        when(organization.getUuid()).thenReturn(UUID.randomUUID());
        when(organization.getActive()).thenReturn(true);
        when(organization.getProviderKey()).thenReturn(providerKey);
        return organization;
    }
}
