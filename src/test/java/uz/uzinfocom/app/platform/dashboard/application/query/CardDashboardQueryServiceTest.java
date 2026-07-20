package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardDailyCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CardDashboardResponse;
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

/** Unit-level only: verifies delegation to {@link CardStatsQueryService} for the card module's own dashboard. */
class CardDashboardQueryServiceTest {

    private final CardStatsQueryService cardStatsQueryService = mock(CardStatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);

    private final CardDashboardQueryService service = new CardDashboardQueryService(
            cardStatsQueryService,
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
    void dashboardDelegatesToCardStatsQueryService() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(cardStatsQueryService.countTotal()).thenReturn(10L);
        when(cardStatsQueryService.countActive()).thenReturn(4L);
        when(cardStatsQueryService.countByStatus()).thenReturn(List.of(
                new CardStatusCountResponse(CardStatus.NEW, 4L),
                new CardStatusCountResponse(CardStatus.APPROVED, 6L)
        ));
        when(cardStatsQueryService.countByType()).thenReturn(List.of(
                new CardTypeCountResponse(CardType.CARD161, 10L)
        ));

        LocalDate to = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);
        when(cardStatsQueryService.countByMonth(from, to)).thenReturn(List.of(
                new CardDailyCountResponse(from, 3L)
        ));

        CardDashboardResponse response = service.getDashboard();

        assertThat(response.total()).isEqualTo(10L);
        assertThat(response.active()).isEqualTo(4L);
        assertThat(response.byStatus()).hasSize(2);
        assertThat(response.byType()).hasSize(1);
        assertThat(response.dynamics().from()).isEqualTo(from);
        assertThat(response.dynamics().to()).isEqualTo(to);
        assertThat(response.dynamics().points()).hasSize(1);
        assertThat(response.dynamics().points().get(0).count()).isEqualTo(3L);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }
}
