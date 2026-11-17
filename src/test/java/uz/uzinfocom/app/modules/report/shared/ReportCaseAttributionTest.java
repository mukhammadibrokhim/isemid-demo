package uz.uzinfocom.app.modules.report.shared;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportCaseAttributionTest {

    private final OrganizationScopeResolver resolver = mock(OrganizationScopeResolver.class);
    private final ReportCaseAttribution attribution = new ReportCaseAttribution(resolver);

    @AfterEach
    void clear() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void territorialScopeAttributesToReceiver() {
        givenScope(OrganizationScopeMode.DISTRICT);
        assertThat(attribution.byReceiver()).isTrue();
    }

    @Test
    void singleOrganizationScopeStaysOnSender() {
        givenScope(OrganizationScopeMode.ORGANIZATION);
        assertThat(attribution.byReceiver()).isFalse();
    }

    @Test
    void noCurrentOrganizationStaysOnSender() {
        assertThat(attribution.byReceiver()).isFalse();
    }

    @Test
    void applyRenamesTheWholeSenderTokenOnlyForTerritorialScope() {
        String sql = "select f.sender_organization_id from form058 f group by f.sender_organization_id";

        givenScope(OrganizationScopeMode.REGION);
        assertThat(attribution.apply(sql))
                .isEqualTo("select f.receiver_organization_id from form058 f group by f.receiver_organization_id");

        givenScope(OrganizationScopeMode.ORGANIZATION);
        assertThat(attribution.apply(sql)).isEqualTo(sql);
    }

    private void givenScope(OrganizationScopeMode mode) {
        Organization organization = new Organization();
        CurrentOrganizationContext.set(organization);
        ResolvedOrganizationScope scope = new ResolvedOrganizationScope(mode, 1L, null, null, null, null, null);
        when(resolver.resolve(any())).thenReturn(scope);
    }
}
