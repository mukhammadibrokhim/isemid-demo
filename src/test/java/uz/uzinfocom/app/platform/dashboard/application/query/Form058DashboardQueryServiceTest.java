package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.stats.query.Form058StatsQueryService;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058DailyCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058OrganizationCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058SourceCountResponse;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.Form058DashboardResponse;
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
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope-mode branching and correct delegation to
 * {@link Form058StatsQueryService} - matching {@code
 * HomeDashboardQueryServiceTest}'s established depth. This service never
 * touches form058_1 at all, so unlike the combined home dashboard there is
 * no cross-table merge to verify here.
 */
class Form058DashboardQueryServiceTest {

    private final Form058StatsQueryService form058StatsQueryService = mock(Form058StatsQueryService.class);
    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final OrganizationNameResolver organizationNameResolver = mock(OrganizationNameResolver.class);

    private final Form058DashboardQueryService service = new Form058DashboardQueryService(
            form058StatsQueryService,
            organizationScopeResolver,
            organizationRepository,
            districtRepository,
            regionRepository,
            referenceLookupService,
            organizationNameResolver,
            Runnable::run
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
    void summaryDelegatesToDirectCountMethods() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form058StatsQueryService.countTotal(Form058Direction.INCOMING)).thenReturn(10L);
        when(form058StatsQueryService.countActive(Form058Direction.INCOMING)).thenReturn(4L);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tashkent"));
        when(form058StatsQueryService.countByDay(Form058Direction.INCOMING, today, today)).thenReturn(List.of(
                new Form058DailyCountResponse(today, 2L)
        ));

        Form058DashboardResponse response = service.getDashboard();

        assertThat(response.total()).isEqualTo(10L);
        assertThat(response.active()).isEqualTo(4L);
        assertThat(response.newCasesToday()).isEqualTo(2L);
        assertThat(response.asOfDate()).isEqualTo(today);
    }

    @Test
    void topDiagnosesAndSourceBreakdownMapFromForm058OwnDtos() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        when(form058StatsQueryService.topMkb10(Form058Direction.INCOMING, 5)).thenReturn(List.of(
                new Form058Mkb10CountResponse("A82", 10L)
        ));
        when(form058StatsQueryService.countBySource(Form058Direction.INCOMING)).thenReturn(List.of(
                new Form058SourceCountResponse("MANUAL", 7L),
                new Form058SourceCountResponse("QR", 9L)
        ));

        Form058DashboardResponse response = service.getDashboard();

        assertThat(response.topDiagnoses()).hasSize(1);
        assertThat(response.topDiagnoses().getFirst().mkb10Code()).isEqualTo("A82");
        assertThat(response.topDiagnoses().getFirst().count()).isEqualTo(10L);

        assertThat(response.sourceBreakdown()).hasSize(2);
        assertThat(response.sourceBreakdown().getFirst().source()).isEqualTo("QR");
        assertThat(response.sourceBreakdown().getFirst().count()).isEqualTo(9L);
    }

    @Test
    void districtScopeBuildsOrganizationBreakdown() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.DISTRICT, "REGION1", "DISTRICT1"));

        OrganizationNameProjection org = new OrganizationNameProjection(10L, "Org 10", null, null, null, null);
        when(organizationRepository.findActiveByDistrictCode("DISTRICT1")).thenReturn(List.of(org));
        when(form058StatsQueryService.countByReceiverOrganizationWithinIds(List.of(10L)))
                .thenReturn(List.of(new Form058OrganizationCountResponse(10L, 5L)));
        when(organizationNameResolver.resolve(any(OrganizationLocalizedName.class))).thenReturn("Поликлиника №10");

        Form058DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).hasSize(1);
        assertThat(response.geoBreakdown().getFirst().code()).isEqualTo("10");
        assertThat(response.geoBreakdown().getFirst().name()).isEqualTo("Поликлиника №10");
        assertThat(response.geoBreakdown().getFirst().count()).isEqualTo(5L);
    }

    @Test
    void organizationScopeHasNoGeoBreakdown() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        Form058DashboardResponse response = service.getDashboard();

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
        when(form058StatsQueryService.countByReceiverOrganizationWithinIds(List.of(10L)))
                .thenReturn(List.of(new Form058OrganizationCountResponse(10L, 7L)));
        when(referenceLookupService.getDistrictName("DISTRICT1")).thenReturn("Район 1");
        when(referenceLookupService.getDistrictName("DISTRICT2")).thenReturn("Район 2");

        Form058DashboardResponse response = service.getDashboard();

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

        Form058DashboardResponse response = service.getDashboard();

        assertThat(response.geoBreakdown()).hasSize(1);
        assertThat(response.geoBreakdown().getFirst().count()).isZero();
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
