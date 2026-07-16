package uz.uzinfocom.app.modules.form0581.application.stats.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581StatsRepository;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies the direction-to-{@code received} translation
 * and the admin/super-admin gates. The actual Criteria API query execution
 * (Form0581StatsRepository) is not covered by an automated test — this
 * project has no DB-backed test infrastructure anywhere (no Testcontainers/
 * H2/@DataJpaTest), matching the existing, pre-established testing depth for
 * every other Specification/repository class in the codebase.
 */
class Form0581StatsQueryServiceTest {

    private final Form0581StatsRepository form0581StatsRepository = mock(Form0581StatsRepository.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final AdminAccessGuard adminAccessGuard = mock(AdminAccessGuard.class);

    private final Form0581StatsQueryService service =
            new Form0581StatsQueryService(form0581StatsRepository, organizationScopeResolver, adminAccessGuard);

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void countByStatusOutgoingResolvesReceivedFalse() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        service.countByStatus(Form0581Direction.OUTGOING);

        verify(form0581StatsRepository).countByStatus(scope, false);
    }

    @Test
    void countByStatusIncomingResolvesReceivedTrue() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        service.countByStatus(Form0581Direction.INCOMING);

        verify(form0581StatsRepository).countByStatus(scope, true);
    }

    @Test
    void countByStatusAllRequiresSuperAdminAndPassesNullReceived() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        service.countByStatus(Form0581Direction.ALL);

        verify(adminAccessGuard).requireSuperAdmin();
        verify(form0581StatsRepository).countByStatus(eq(scope), isNull());
    }

    @Test
    void countByStatusAllPropagatesAccessDeniedWhenNotSuperAdmin() {
        CurrentOrganizationContext.set(organization(1L));
        when(organizationScopeResolver.resolve(any())).thenReturn(mock(ResolvedOrganizationScope.class));
        doThrow(new AccessDeniedException("no")).when(adminAccessGuard).requireSuperAdmin();

        assertThatThrownBy(() -> service.countByStatus(Form0581Direction.ALL))
                .isInstanceOf(AccessDeniedException.class);

        verify(form0581StatsRepository, never()).countByStatus(any(), any());
    }

    @Test
    void countByStatusThrowsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(() -> service.countByStatus(Form0581Direction.OUTGOING))
                .isInstanceOf(Form0581ScopeViolationException.class);
    }

    @Test
    void countBySenderOrganizationRequiresAdmin() {
        doThrow(new AccessDeniedException("no")).when(adminAccessGuard).requireAdmin();

        assertThatThrownBy(service::countBySenderOrganization)
                .isInstanceOf(AccessDeniedException.class);

        verify(form0581StatsRepository, never()).countBySenderOrganization();
    }

    @Test
    void countByReceiverOrganizationDelegatesAfterAdminCheck() {
        service.countByReceiverOrganization();

        verify(adminAccessGuard).requireAdmin();
        verify(form0581StatsRepository).countByReceiverOrganization();
    }

    @Test
    void countByMonthIncomingResolvesReceivedTrue() {
        CurrentOrganizationContext.set(organization(1L));
        ResolvedOrganizationScope scope = mock(ResolvedOrganizationScope.class);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        service.countByMonth(Form0581Direction.INCOMING, from, to);

        verify(form0581StatsRepository).countByMonth(scope, true, from, to);
    }

    @Test
    void countByReceiverOrganizationWithinIdsRequiresNoAdminCheck() {
        service.countByReceiverOrganizationWithinIds(List.of(1L, 2L));

        verify(adminAccessGuard, never()).requireAdmin();
        verify(form0581StatsRepository).countByReceiverOrganizationWithinIds(List.of(1L, 2L));
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }
}
