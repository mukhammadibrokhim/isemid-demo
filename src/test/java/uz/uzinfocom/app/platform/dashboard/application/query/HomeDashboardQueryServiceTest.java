package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.form058.application.stats.query.Form058StatsQueryService;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058OrganizationCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058StatusCountResponse;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
import uz.uzinfocom.app.modules.form0581.application.stats.query.Form0581StatsQueryService;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581StatusCountResponse;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
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
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope-mode branching (geo breakdown, medical
 * institutions count) and the merge logic (case summary, dynamics, top
 * diagnoses). Every module dependency here is mocked at its public
 * query-service boundary ({@code Form058StatsQueryService} etc.), matching
 * how {@code HomeDashboardQueryService} itself only depends on those
 * services, never on another module's repository. The underlying Criteria
 * API queries are not covered here — matching the existing, pre-established
 * testing depth for every other stats repository in this codebase (no
 * Testcontainers/H2/@DataJpaTest anywhere in the project).
 */
class HomeDashboardQueryServiceTest {

    private final Form058StatsQueryService form058StatsQueryService = mock(Form058StatsQueryService.class);
    private final Form0581StatsQueryService form0581StatsQueryService = mock(Form0581StatsQueryService.class);
    private final CardStatsQueryService cardStatsQueryService = mock(CardStatsQueryService.class);
    private final ActStatsQueryService actStatsQueryService = mock(ActStatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver =
            mock(OrganizationScopeOrganizationIdResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);

    private final HomeDashboardQueryService service = new HomeDashboardQueryService(
            form058StatsQueryService,
            form0581StatsQueryService,
            cardStatsQueryService,
            actStatsQueryService,
            organizationScopeResolver,
            organizationScopeOrganizationIdResolver,
            organizationRepository,
            districtRepository,
            regionRepository,
            referenceLookupService
    );

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
        verify(districtRepository, never()).findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(any());
        verify(regionRepository, never()).findAllByDeletedFalseOrderByNameUzAsc();
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

        District coveredDistrict = district("DISTRICT1");
        District emptyDistrict = district("DISTRICT2");
        when(districtRepository.findAllByParentCodeAndDeletedFalseOrderByNameUzAsc("REGION1"))
                .thenReturn(List.of(coveredDistrict, emptyDistrict));

        when(organizationRepository.findActiveIdAndDistrictCodeByRegionCode("REGION1"))
                .thenReturn(List.of(new OrganizationGeoProjection(10L, "DISTRICT1")));

        when(form058StatsQueryService.countByReceiverOrganizationWithinIds(List.of(10L)))
                .thenReturn(List.of(new Form058OrganizationCountResponse(10L, 7L)));

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
        when(regionRepository.findAllByDeletedFalseOrderByNameUzAsc()).thenReturn(List.of(region("REGION1")));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutionsCount()).isEqualTo(1245L);
        assertThat(response.geoBreakdown()).hasSize(1);
        verify(organizationScopeOrganizationIdResolver, never()).resolveScopeOrganizationIds(any(), any(), any());
    }

    @Test
    void caseSummaryCombinesForm058AndForm0581() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form058StatsQueryService.countByStatus(Form058Direction.INCOMING)).thenReturn(List.of(
                new Form058StatusCountResponse(FormStatus.SENT, 5L),
                new Form058StatusCountResponse(FormStatus.APPROVED, 2L)
        ));
        when(form0581StatsQueryService.countByStatus(Form0581Direction.INCOMING)).thenReturn(List.of(
                new Form0581StatusCountResponse(Form0581Status.RECEIVED, 3L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.caseSummary().form058Total()).isEqualTo(7L);
        assertThat(response.caseSummary().form0581Total()).isEqualTo(3L);
        assertThat(response.caseSummary().totalCases()).isEqualTo(10L);
        assertThat(response.caseSummary().activeCases()).isEqualTo(5L + 3L);
    }

    @Test
    void topDiagnosesMergeAndSortAcrossBothFormTypes() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form058StatsQueryService.topMkb10(Form058Direction.INCOMING, 20)).thenReturn(List.of(
                new uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse("A00", 3L),
                new uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse("B00", 10L)
        ));
        when(form0581StatsQueryService.topMkb10(Form0581Direction.INCOMING, 20)).thenReturn(List.of(
                new uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581Mkb10CountResponse("A00", 4L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.topDiagnoses()).hasSize(2);
        assertThat(response.topDiagnoses().get(0).mkb10Code()).isEqualTo("B00");
        assertThat(response.topDiagnoses().get(0).count()).isEqualTo(10L);
        assertThat(response.topDiagnoses().get(1).mkb10Code()).isEqualTo("A00");
        assertThat(response.topDiagnoses().get(1).count()).isEqualTo(7L);
    }

    @Test
    void cardActiveCountExcludesApprovedOnly() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(cardStatsQueryService.countByStatus()).thenReturn(List.of(
                new CardStatusCountResponse(CardStatus.NEW, 4L),
                new CardStatusCountResponse(CardStatus.APPROVED, 6L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.cardStats().total()).isEqualTo(10L);
        assertThat(response.cardStats().active()).isEqualTo(4L);
    }

    @Test
    void actTotalSumsAllStatuses() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(actStatsQueryService.countByStatus()).thenReturn(List.of(
                new ActStatusCountResponse(ActStatus.NEW, 2L),
                new ActStatusCountResponse(ActStatus.COMPLETED, 5L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.actStats().total()).isEqualTo(7L);
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
