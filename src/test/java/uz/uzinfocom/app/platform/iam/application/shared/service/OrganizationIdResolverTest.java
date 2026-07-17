package uz.uzinfocom.app.platform.iam.application.shared.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.iam.application.shared.exception.OrganizationResolutionException;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrganizationIdResolverTest {

    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final OrganizationIdResolver resolver = new OrganizationIdResolver(organizationRepository);

    @Test
    void resolvesOnlyActiveOrganizationIdByUuid() {
        UUID uuid = UUID.randomUUID();
        when(organizationRepository.findActiveIdByUuid(uuid)).thenReturn(Optional.of(42L));

        assertThat(resolver.resolveActiveId(uuid)).isEqualTo(42L);
        verify(organizationRepository).findActiveIdByUuid(uuid);
    }

    @Test
    void rejectsUnknownOrganizationUuid() {
        UUID uuid = UUID.randomUUID();
        when(organizationRepository.findActiveIdByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveActiveId(uuid))
                .isInstanceOf(OrganizationResolutionException.class);
    }
}
