package uz.uzinfocom.app.modules.card.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.query.mapper.CardTableMapper;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058PdfMapper;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code CardFilterRequestTest} covers the actual filter-building logic
 * (pure, mockless). This test covers {@code findMine}'s two branches: a
 * personal (assignedToUserId-forced) view for a DISTRICT/ORGANIZATION-scope
 * account, and an organization-wide view (no user filter forced) for a
 * broader-scope (REGION/ALL) account — plus the auth gate for the personal
 * branch and the organization-selection gate shared by both.
 */
class CardQueryServiceScopedViewsTest {

    private CardRepository cardRepository;
    private CurrentUserProvider currentUserProvider;
    private OrganizationScopeResolver organizationScopeResolver;
    private CardQueryService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        CardTableMapper cardTableMapper = mock(CardTableMapper.class);
        CardTypeHandlerRegistry handlerRegistry = mock(CardTypeHandlerRegistry.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        organizationScopeResolver = mock(OrganizationScopeResolver.class);
        SenderReceiverScopePredicateFactory scopePredicateFactory = mock(SenderReceiverScopePredicateFactory.class);
        Form058PdfMapper form058PdfMapper = mock(Form058PdfMapper.class);

        service = new CardQueryService(
                cardRepository, cardTableMapper, handlerRegistry,
                currentUserProvider, organizationScopeResolver, scopePredicateFactory, form058PdfMapper
        );

        when(cardRepository.findBy(any(Specification.class), any(Function.class)))
                .thenReturn(Page.empty());
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void findMineRunsForAnAuthenticatedUserInOrganizationScope() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.ORGANIZATION));
        when(currentUserProvider.userIdOrNull()).thenReturn(42L);

        service.findMine(emptyFilter());

        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineRefusesAnUnauthenticatedCallerInOrganizationScope() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.ORGANIZATION));
        when(currentUserProvider.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.findMine(emptyFilter()))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void findMineRefusesWhenNoOrganizationIsSelected() {
        assertThatThrownBy(() -> service.findMine(emptyFilter()))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void findMineStaysPersonalForDistrictLevelAccount() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.DISTRICT));
        when(currentUserProvider.userIdOrNull()).thenReturn(42L);

        service.findMine(emptyFilter());

        verify(currentUserProvider, times(1)).userIdOrNull();
        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineWidensToOrganizationScopeForRegionLevelAccountWithoutRequiringAnAttachedUser() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.REGION));

        service.findMine(emptyFilter());

        verify(currentUserProvider, never()).userIdOrNull();
        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineWidensToOrganizationScopeForRepublicLevelAccountWithoutRequiringAnAttachedUser() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scopeWith(OrganizationScopeMode.ALL));

        service.findMine(emptyFilter());

        verify(currentUserProvider, never()).userIdOrNull();
        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    private ResolvedOrganizationScope scopeWith(OrganizationScopeMode mode) {
        return new ResolvedOrganizationScope(mode, 1L, null, null, null, null, null);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }

    private CardFilterRequest emptyFilter() {
        return new CardFilterRequest(1, 20, null, null, null, null, null, null, null);
    }
}
