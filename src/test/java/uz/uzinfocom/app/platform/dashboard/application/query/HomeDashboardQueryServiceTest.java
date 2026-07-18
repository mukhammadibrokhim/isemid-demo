package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDailyCountResponse;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardDailyCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.SourceCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesGranularity;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TopDiagnosisResponse;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.CaseStatsAggregateRepository;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.CaseSummaryAggregate;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.GeoCodeCount;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope-mode branching (geo breakdown, medical
 * institutions count) and correct delegation to {@link
 * CaseStatsAggregateRepository} for every cross-form-type case statistic —
 * that repository owns the actual form058+form058_1 SQL merge now, so these
 * tests only need to confirm this service passes it the right scope/window
 * and maps its result correctly, not re-verify a merge that no longer
 * happens in Java. Card/Act dependencies are still mocked at their own
 * public query-service boundary, since those metrics are single-table (no
 * sibling form to combine with). The underlying SQL queries are not covered
 * here — matching the existing, pre-established testing depth for every
 * other stats repository in this codebase (no Testcontainers/H2/@DataJpaTest
 * anywhere in the project).
 */
class HomeDashboardQueryServiceTest {

    private final CaseStatsAggregateRepository caseStatsAggregateRepository = mock(CaseStatsAggregateRepository.class);
    private final CardStatsQueryService cardStatsQueryService = mock(CardStatsQueryService.class);
    private final ActStatsQueryService actStatsQueryService = mock(ActStatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver =
            mock(OrganizationScopeOrganizationIdResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);

    /**
     * Runs each "async" branch inline on the calling (test) thread —
     * {@code getHome()} fans its 9 independent aggregations out onto an
     * {@link java.util.concurrent.Executor} in production, but a
     * same-thread executor here keeps these tests deterministic and lets
     * them exercise the exact same code path without any real concurrency.
     */
    private final HomeDashboardQueryService service = new HomeDashboardQueryService(
            caseStatsAggregateRepository,
            cardStatsQueryService,
            actStatsQueryService,
            organizationScopeResolver,
            organizationScopeOrganizationIdResolver,
            organizationRepository,
            referenceLookupService,
            Runnable::run
    );

    /**
     * {@code buildCaseSummary} runs unconditionally on every {@code
     * getHome()} call, so every test needs some stubbed result for it, even
     * ones that don't care about its value — otherwise the unstubbed mock
     * would return null (Mockito only auto-empties collection-typed
     * returns, not plain objects) and the service would NPE unpacking it.
     */
    @BeforeEach
    void stubCaseSummaryDefault() {
        when(caseStatsAggregateRepository.caseSummary(any(), any()))
                .thenReturn(new CaseSummaryAggregate(0, 0, 0, 0));
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void throwsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(service::getHome).isInstanceOf(ScopeViolationException.class);
    }

    @Test
    void districtScopeHasNoGeoBreakdownAndReportsOwnInstitutionCount() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.DISTRICT, "REGION1", "DISTRICT1");
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);
        when(organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                OrganizationScopeMode.DISTRICT, "REGION1", "DISTRICT1"
        )).thenReturn(List.of(1L, 2L, 3L));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.geoBreakdown()).isEmpty();
        assertThat(response.medicalInstitutionsCount()).isEqualTo(3L);
        verify(caseStatsAggregateRepository, never()).districtBreakdown(any());
        verify(caseStatsAggregateRepository, never()).regionBreakdown();
    }

    @Test
    void organizationScopeReportsSingleInstitutionAndNoBreakdown() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        HomeDashboardResponse response = service.getHome();

        assertThat(response.geoBreakdown()).isEmpty();
        assertThat(response.medicalInstitutionsCount()).isEqualTo(1L);
    }

    @Test
    void regionScopeBuildsDistrictBreakdownZeroFilledForMissingDistricts() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.REGION, "REGION1", null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(caseStatsAggregateRepository.districtBreakdown("REGION1")).thenReturn(List.of(
                new GeoCodeCount("DISTRICT1", 7L),
                new GeoCodeCount("DISTRICT2", 0L)
        ));
        when(referenceLookupService.getDistrictName("DISTRICT1")).thenReturn("Район 1");
        when(referenceLookupService.getDistrictName("DISTRICT2")).thenReturn("Район 2");

        HomeDashboardResponse response = service.getHome();

        assertThat(response.geoBreakdown()).hasSize(2);
        assertThat(response.geoBreakdown())
                .filteredOn(item -> item.code().equals("DISTRICT1"))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.count()).isEqualTo(7L);
                    assertThat(item.name()).isEqualTo("Район 1");
                });
        assertThat(response.geoBreakdown())
                .filteredOn(item -> item.code().equals("DISTRICT2"))
                .singleElement()
                .satisfies(item -> assertThat(item.count()).isZero());
    }

    @Test
    void allScopeUsesNationwideInstitutionCountAndRegionBreakdown() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ALL, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);
        when(organizationRepository.countByActiveTrue()).thenReturn(1245L);
        when(caseStatsAggregateRepository.regionBreakdown()).thenReturn(List.of(new GeoCodeCount("REGION1", 42L)));
        when(referenceLookupService.getRegionName("REGION1")).thenReturn("Область 1");

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutionsCount()).isEqualTo(1245L);
        assertThat(response.geoBreakdown()).hasSize(1);
        assertThat(response.geoBreakdown().get(0).count()).isEqualTo(42L);
        verify(organizationScopeOrganizationIdResolver, never()).resolveScopeOrganizationIds(any(), any(), any());
    }

    @Test
    void caseSummaryReflectsAggregateRepositoryResult() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(caseStatsAggregateRepository.caseSummary(any(), any()))
                .thenReturn(new CaseSummaryAggregate(7L, 3L, 8L, 2L));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.caseSummary().form058Total()).isEqualTo(7L);
        assertThat(response.caseSummary().form0581Total()).isEqualTo(3L);
        assertThat(response.caseSummary().totalCases()).isEqualTo(10L);
        assertThat(response.caseSummary().activeCases()).isEqualTo(8L);
        assertThat(response.caseSummary().newCasesToday()).isEqualTo(2L);
    }

    @Test
    void topDiagnosesDelegatesToAggregateRepositoryWithResultLimitOfFive() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(caseStatsAggregateRepository.topDiagnoses(scope, 5)).thenReturn(List.of(
                new TopDiagnosisResponse("B00", 10L),
                new TopDiagnosisResponse("A00", 7L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.topDiagnoses()).hasSize(2);
        assertThat(response.topDiagnoses().get(0).mkb10Code()).isEqualTo("B00");
        assertThat(response.topDiagnoses().get(0).count()).isEqualTo(10L);
        assertThat(response.topDiagnoses().get(1).mkb10Code()).isEqualTo("A00");
        assertThat(response.topDiagnoses().get(1).count()).isEqualTo(7L);
    }

    @Test
    void cardStatsReflectDirectTotalAndActiveCounts() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(cardStatsQueryService.countTotal()).thenReturn(10L);
        when(cardStatsQueryService.countActive()).thenReturn(4L);

        HomeDashboardResponse response = service.getHome();

        assertThat(response.cardStats().total()).isEqualTo(10L);
        assertThat(response.cardStats().active()).isEqualTo(4L);
    }

    @Test
    void actStatsReflectDirectTotalCount() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(actStatsQueryService.countTotal()).thenReturn(7L);

        HomeDashboardResponse response = service.getHome();

        assertThat(response.actStats().total()).isEqualTo(7L);
    }

    @Test
    void responseCarriesAGeneratedAtTimestampCloseToNow() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.generatedAt()).isNotNull();
        assertThat(response.generatedAt()).isCloseTo(Instant.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    void dynamicsCarriesTheExplicitCalendarYearWindowItActuallyQueried() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        HomeDashboardResponse response = service.getHome();

        LocalDate expectedTo = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        LocalDate expectedFrom = LocalDate.of(expectedTo.getYear(), 1, 1);

        assertThat(response.dynamics().from()).isEqualTo(expectedFrom);
        assertThat(response.dynamics().to()).isEqualTo(expectedTo);
        assertThat(response.dynamics().granularity()).isEqualTo(TimeSeriesGranularity.MONTH);
        verify(caseStatsAggregateRepository).monthlyDynamics(scope, expectedFrom, expectedTo);
    }

    @Test
    void caseSummaryAsOfDateMatchesTheDayItWasQueriedFor() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        HomeDashboardResponse response = service.getHome();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        assertThat(response.caseSummary().asOfDate()).isEqualTo(today);
        verify(caseStatsAggregateRepository).caseSummary(scope, today);
    }

    @Test
    void cardAndActDynamicsShareTheSameWindowAsCaseDynamicsAndReflectQueriedData() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        LocalDate to = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);

        when(cardStatsQueryService.countByMonth(from, to)).thenReturn(List.of(
                new CardDailyCountResponse(from, 3L)
        ));
        when(actStatsQueryService.countByMonth(from, to)).thenReturn(List.of(
                new ActDailyCountResponse(from, 2L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.cardStats().dynamics().from()).isEqualTo(from);
        assertThat(response.cardStats().dynamics().to()).isEqualTo(to);
        assertThat(response.cardStats().dynamics().points()).hasSize(1);
        assertThat(response.cardStats().dynamics().points().get(0).count()).isEqualTo(3L);

        assertThat(response.actStats().dynamics().from()).isEqualTo(from);
        assertThat(response.actStats().dynamics().to()).isEqualTo(to);
        assertThat(response.actStats().dynamics().points()).hasSize(1);
        assertThat(response.actStats().dynamics().points().get(0).count()).isEqualTo(2L);
    }

    @Test
    void sourceBreakdownDelegatesToAggregateRepository() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(caseStatsAggregateRepository.sourceBreakdown(scope)).thenReturn(List.of(
                new SourceCountResponse("QR", 10L),
                new SourceCountResponse("MANUAL", 7L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.sourceBreakdown()).hasSize(2);
        assertThat(response.sourceBreakdown().get(0).source()).isEqualTo("QR");
        assertThat(response.sourceBreakdown().get(0).count()).isEqualTo(10L);
        assertThat(response.sourceBreakdown().get(1).source()).isEqualTo("MANUAL");
        assertThat(response.sourceBreakdown().get(1).count()).isEqualTo(7L);
    }

    private ResolvedOrganizationScope scopeOf(OrganizationScopeMode mode, String regionCode, String districtCode) {
        return new ResolvedOrganizationScope(mode, 1L, null, null, null, regionCode, districtCode);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }
}
