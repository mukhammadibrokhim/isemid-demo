package uz.uzinfocom.app.modules.act.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActStatsRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope resolution and delegation to
 * {@link ActStatsRepository}. The actual Criteria API query execution is
 * not covered — matching the existing, pre-established testing depth for
 * every other stats repository in this codebase.
 */
class ActStatsQueryServiceTest {

    private final ActStatsRepository actStatsRepository = mock(ActStatsRepository.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);

    private final ActStatsQueryService service =
            new ActStatsQueryService(actStatsRepository, organizationScopeResolver);

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void countByStatusResolvesScopeAndDelegates() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        service.countByStatus();

        verify(actStatsRepository).countByStatus(scope);
    }

    @Test
    void countByMonthResolvesScopeAndDelegates() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 7, 18);

        service.countByMonth(from, to);

        verify(actStatsRepository).countByMonth(scope, from, to);
    }

    @Test
    void countByStatusThrowsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(service::countByStatus).isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void countTotalResolvesScopeAndDelegates() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        service.countTotal();

        verify(actStatsRepository).countTotal(scope);
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
