package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.form0581.application.stats.query.Form0581StatsQueryService;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581Mkb10CountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581MonthlyOutcomeCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581OrganizationCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581SourceCountResponse;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.Form0581DashboardResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationNameProjection;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope-mode branching and correct delegation to
 * {@link Form0581StatsQueryService} - see {@code
 * Form058DashboardQueryServiceTest} for the mirrored form058 coverage.
 */
class Form0581DashboardQueryServiceTest {

    private final Form0581StatsQueryService form0581StatsQueryService = mock(Form0581StatsQueryService.class);
    private final CardStatsQueryService cardStatsQueryService = mock(CardStatsQueryService.class);
    private final ActStatsQueryService actStatsQueryService = mock(ActStatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final OrganizationNameResolver organizationNameResolver = mock(OrganizationNameResolver.class);
    private final SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);

    private final Form0581DashboardQueryService service = new Form0581DashboardQueryService(
            form0581StatsQueryService,
            cardStatsQueryService,
            actStatsQueryService,
            organizationScopeResolver,
            organizationRepository,
            districtRepository,
            regionRepository,
            referenceLookupService,
            organizationNameResolver,
            Runnable::run,
            systemSettingResolver
    );

    {
        when(systemSettingResolver.resolveLong(anyString(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void throwsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(service::getDashboard).isInstanceOf(ScopeViolationException.class);
    }

    @Test
    void summaryDelegatesToDirectCountMethods() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form0581StatsQueryService.countTotal(Form0581Direction.INCOMING)).thenReturn(10L);
        when(form0581StatsQueryService.countActive(Form0581Direction.INCOMING)).thenReturn(4L);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        when(form0581StatsQueryService.countByDay(Form0581Direction.INCOMING, today, today)).thenReturn(List.of(
                new Form0581DailyCountResponse(today, 2L)
        ));

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.total()).isEqualTo(10L);
        assertThat(response.active()).isEqualTo(4L);
        assertThat(response.newCasesToday()).isEqualTo(2L);
        assertThat(response.asOfDate()).isEqualTo(today);
    }

    @Test
    void dynamicsMapsMonthlyOutcomeBreakdownIncludingCanceledAndApproved() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        LocalDate periodStart = LocalDate.of(2026, 7, 1);
        when(form0581StatsQueryService.countByMonthWithOutcomes(eq(Form0581Direction.INCOMING), any(), any())).thenReturn(List.of(
                new Form0581MonthlyOutcomeCountResponse(periodStart, 10L, 3L, 5L)
        ));

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.dynamics().points()).hasSize(1);
        DynamicsPointResponse point = response.dynamics().points().getFirst();
        assertThat(point.periodStart()).isEqualTo(periodStart);
        assertThat(point.count()).isEqualTo(10L);
        assertThat(point.canceledCount()).isEqualTo(3L);
        assertThat(point.approvedCount()).isEqualTo(5L);
    }

    @Test
    void topDiagnosesAndSourceBreakdownMapFromForm0581OwnDtos() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form0581StatsQueryService.topMkb10(Form0581Direction.INCOMING, 5)).thenReturn(List.of(
                new Form0581Mkb10CountResponse("A82", 10L)
        ));
        when(form0581StatsQueryService.countBySource(Form0581Direction.INCOMING)).thenReturn(List.of(
                new Form0581SourceCountResponse("MANUAL", 7L),
                new Form0581SourceCountResponse("QR", 9L)
        ));

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.topDiagnoses()).hasSize(1);
        assertThat(response.topDiagnoses().get(0).mkb10Code()).isEqualTo("A82");
        assertThat(response.topDiagnoses().get(0).count()).isEqualTo(10L);

        assertThat(response.sourceBreakdown()).hasSize(2);
        assertThat(response.sourceBreakdown().get(0).source()).isEqualTo("QR");
        assertThat(response.sourceBreakdown().get(0).count()).isEqualTo(9L);
    }

    @Test
    void districtScopeBuildsOrganizationBreakdown() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.DISTRICT, "REGION1", "DISTRICT1"));

        OrganizationNameProjection org = new OrganizationNameProjection(10L, "Org 10", null, null, null, null);
        when(organizationRepository.findActiveByDistrictCode("DISTRICT1")).thenReturn(List.of(org));
        when(form0581StatsQueryService.countByReceiverOrganizationWithinIds(List.of(10L)))
                .thenReturn(List.of(new Form0581OrganizationCountResponse(10L, 5L)));
        when(organizationNameResolver.resolve(any(OrganizationLocalizedName.class))).thenReturn("Поликлиника №10");

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).hasSize(1);
        assertThat(response.geoBreakdown().get(0).code()).isEqualTo("10");
        assertThat(response.geoBreakdown().get(0).name()).isEqualTo("Поликлиника №10");
        assertThat(response.geoBreakdown().get(0).count()).isEqualTo(5L);
    }

    @Test
    void organizationScopeHasNoGeoBreakdown() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).isEmpty();
    }

    @Test
    void regionScopeBuildsDistrictBreakdownZeroFilledForMissingDistricts() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.REGION, "REGION1", null));

        District covered = district("DISTRICT1");
        District empty = district("DISTRICT2");
        when(districtRepository.findAllByParentCodeAndDeletedFalseOrderByNameUzAsc("REGION1"))
                .thenReturn(List.of(covered, empty));
        when(organizationRepository.findActiveIdAndDistrictCodeByRegionCode("REGION1"))
                .thenReturn(List.of(new OrganizationGeoProjection(10L, "DISTRICT1")));
        when(form0581StatsQueryService.countByReceiverOrganizationWithinIds(List.of(10L)))
                .thenReturn(List.of(new Form0581OrganizationCountResponse(10L, 7L)));
        when(referenceLookupService.getDistrictName("DISTRICT1")).thenReturn("Район 1");
        when(referenceLookupService.getDistrictName("DISTRICT2")).thenReturn("Район 2");

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).hasSize(2);
        assertThat(response.geoBreakdown())
                .filteredOn(item -> item.code().equals("DISTRICT1"))
                .singleElement()
                .satisfies(item -> assertThat(item.count()).isEqualTo(7L));
        assertThat(response.geoBreakdown())
                .filteredOn(item -> item.code().equals("DISTRICT2"))
                .singleElement()
                .satisfies(item -> assertThat(item.count()).isZero());
    }

    @Test
    void allScopeBuildsRegionBreakdown() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ALL, null, null));

        when(regionRepository.findAllByDeletedFalseOrderByNameUzAsc()).thenReturn(List.of(region("REGION1")));
        when(referenceLookupService.getRegionName("REGION1")).thenReturn("Область 1");

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).hasSize(1);
        assertThat(response.geoBreakdown().get(0).count()).isZero();
    }

    @Test
    void cardAndActBreakdownsAreScopedToForm0581Only() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(cardStatsQueryService.countTotal(CaseFormType.FORM0581)).thenReturn(2L);
        when(cardStatsQueryService.countByStatus(CaseFormType.FORM0581))
                .thenReturn(List.of(new CardStatusCountResponse(CardStatus.NEW, 2L)));
        when(actStatsQueryService.countTotal(CaseFormType.FORM0581)).thenReturn(1L);
        when(actStatsQueryService.countByStatus(CaseFormType.FORM0581))
                .thenReturn(List.of(new ActStatusCountResponse(ActStatus.NEW, 1L)));

        Form0581DashboardResponse response = service.getDashboard();

        assertThat(response.cardsTotal()).isEqualTo(2L);
        assertThat(response.cardsByStatus()).containsExactly(new CardStatusCountResponse(CardStatus.NEW, 2L));
        assertThat(response.actsTotal()).isEqualTo(1L);
        assertThat(response.actsByStatus()).containsExactly(new ActStatusCountResponse(ActStatus.NEW, 1L));
    }

    private ResolvedOrganizationScope scopeOf(OrganizationScopeMode mode, String regionCode, String districtCode) {
        return new ResolvedOrganizationScope(mode, 1L, null, null, null, regionCode, districtCode);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }

    private District district(String code) {
        District district = new District();
        district.setCode(code);
        return district;
    }

    private Region region(String code) {
        Region region = new Region();
        region.setCode(code);
        return region;
    }
}
