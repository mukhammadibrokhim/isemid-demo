package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDailyCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.ActDashboardResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Unit-level only: verifies delegation to {@link ActStatsQueryService} for the act module's own dashboard. */
class ActDashboardQueryServiceTest {

    private final ActStatsQueryService actStatsQueryService = mock(ActStatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);

    private final ActDashboardQueryService service = new ActDashboardQueryService(
            actStatsQueryService,
            organizationScopeResolver,
            referenceLookupService,
            Runnable::run
    );

    private final ResolvedOrganizationScope scope = new ResolvedOrganizationScope(
            OrganizationScopeMode.ORGANIZATION, 1L, null, null, null, null, null
    );

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void throwsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(service::getDashboard).isInstanceOf(ScopeViolationException.class);
    }

    @Test
    void dashboardDelegatesToActStatsQueryService() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(actStatsQueryService.countTotal()).thenReturn(7L);
        when(actStatsQueryService.countByStatus()).thenReturn(List.of(
                new ActStatusCountResponse(ActStatus.NEW, 2L),
                new ActStatusCountResponse(ActStatus.COMPLETED, 5L)
        ));

        LocalDate to = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);
        when(actStatsQueryService.countByMonth(from, to)).thenReturn(List.of(
                new ActDailyCountResponse(from, 2L)
        ));

        ActDashboardResponse response = service.getDashboard();

        assertThat(response.total()).isEqualTo(7L);
        assertThat(response.byStatus()).hasSize(2);
        assertThat(response.dynamics().from()).isEqualTo(from);
        assertThat(response.dynamics().to()).isEqualTo(to);
        assertThat(response.dynamics().points()).hasSize(1);
        assertThat(response.dynamics().points().get(0).count()).isEqualTo(2L);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }
}
