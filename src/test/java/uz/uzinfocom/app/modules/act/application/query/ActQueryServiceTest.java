package uz.uzinfocom.app.modules.act.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mirrors {@code CardQueryServiceScopedViewsTest}'s auth-gate coverage for
 * {@code findMine} — Act's "mine" view stays personal in every case (no
 * organization-scope broadening; that was only requested for cards) — plus
 * the organization-selection gate for {@code findAll}, the {@code GET
 * /v1/acts} organization-scoped listing.
 */
class ActQueryServiceTest {

    private ActRepository actRepository;
    private CurrentUserProvider currentUserProvider;
    private OrganizationScopeResolver organizationScopeResolver;
    private ActQueryService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        actRepository = mock(ActRepository.class);
        ActMapper actMapper = mock(ActMapper.class);
        ActDetailMapper actDetailMapper = mock(ActDetailMapper.class);
        AuditResolver auditResolver = mock(AuditResolver.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        organizationScopeResolver = mock(OrganizationScopeResolver.class);
        SenderReceiverScopePredicateFactory scopePredicateFactory = mock(SenderReceiverScopePredicateFactory.class);

        service = new ActQueryService(
                actRepository, actMapper, actDetailMapper, auditResolver,
                currentUserProvider, organizationScopeResolver, scopePredicateFactory
        );

        when(actRepository.findBy(any(Specification.class), any(Function.class)))
                .thenReturn(Page.empty());
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void findMineRunsForAnAuthenticatedUser() {
        when(currentUserProvider.userIdOrNull()).thenReturn(42L);

        service.findMine(emptyFilter());

        verify(actRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineRefusesAnUnauthenticatedCaller() {
        when(currentUserProvider.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.findMine(emptyFilter()))
                .isInstanceOf(ActScopeViolationException.class);
    }

    @Test
    void findAllRunsWhenAnOrganizationIsSelected() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.ORGANIZATION));

        service.findAll(emptyFilter());

        verify(actRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findAllRefusesWhenNoOrganizationIsSelected() {
        assertThatThrownBy(() -> service.findAll(emptyFilter()))
                .isInstanceOf(ActScopeViolationException.class);
    }

    private ResolvedOrganizationScope scopeWith(OrganizationScopeMode mode) {
        return new ResolvedOrganizationScope(mode, 1L, null, null, null, null, null);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }

    private ActFilterRequest emptyFilter() {
        return new ActFilterRequest(1, 20, null, null, null, null, null, null);
    }
}
