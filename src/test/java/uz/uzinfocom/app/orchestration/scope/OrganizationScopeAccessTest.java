package uz.uzinfocom.app.orchestration.scope;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopeOrganizationIdResolver;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationScopeAccessTest {

    private final OrganizationScopeOrganizationIdResolver idResolver = mock(OrganizationScopeOrganizationIdResolver.class);
    private final OrganizationScopeAccess access = new OrganizationScopeAccess(idResolver);

    private static ResolvedOrganizationScope scope(MedicalType type, OrganizationScopeMode mode) {
        return new ResolvedOrganizationScope(
                mode, 10L, UUID.randomUUID(), type, OrganizationLevel.REGIONAL, "UZ-TK", "UZ-TK-01"
        );
    }

    @Test
    void nonSanepidOrganizationCoversOnlyItself() {
        ResolvedOrganizationScope scope = scope(MedicalType.HOSPITAL, OrganizationScopeMode.ALL);

        assertThat(access.covers(scope, 10L)).isTrue();
        assertThat(access.covers(scope, 11L)).isFalse();
    }

    @Test
    void sanepidAllScopeCoversEverything() {
        assertThat(access.covers(scope(MedicalType.SANEPID_SERVICE, OrganizationScopeMode.ALL), 999L)).isTrue();
    }

    @Test
    void sanepidOrganizationScopeCoversOnlyItself() {
        ResolvedOrganizationScope scope = scope(MedicalType.SANEPID_SERVICE, OrganizationScopeMode.ORGANIZATION);

        assertThat(access.covers(scope, 10L)).isTrue();
        assertThat(access.covers(scope, 11L)).isFalse();
    }

    @Test
    void sanepidRegionScopeCoversRegionOrganizationsAndItself() {
        ResolvedOrganizationScope scope = scope(MedicalType.SANEPID_SERVICE, OrganizationScopeMode.REGION);
        when(idResolver.resolveScopeOrganizationIds(OrganizationScopeMode.REGION, "UZ-TK", "UZ-TK-01"))
                .thenReturn(List.of(20L, 21L));

        assertThat(access.covers(scope, 20L)).isTrue();
        assertThat(access.covers(scope, 10L)).isTrue();
        assertThat(access.covers(scope, 30L)).isFalse();
    }

    @Test
    void missingScopeOrOrganizationIsNeverCovered() {
        assertThat(access.covers(null, 10L)).isFalse();
        assertThat(access.covers(scope(MedicalType.SANEPID_SERVICE, OrganizationScopeMode.ALL), null)).isFalse();
    }
}
