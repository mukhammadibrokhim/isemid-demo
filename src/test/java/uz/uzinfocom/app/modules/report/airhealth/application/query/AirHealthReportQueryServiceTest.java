package uz.uzinfocom.app.modules.report.airhealth.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.uzinfocom.app.modules.airquality.application.AirQualityNorms;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirBucketStat;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirTerritoryStat;
import uz.uzinfocom.app.modules.airquality.repository.AirAggregateRepository;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthBucket;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthSeriesResponse;
import uz.uzinfocom.app.modules.report.airhealth.application.query.dto.AirHealthTerritoriesResponse;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository.BucketCases;
import uz.uzinfocom.app.modules.report.airhealth.infrastructure.persistence.repository.AirHealthCaseRepository.TerritoryCases;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirHealthReportQueryServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tashkent");

    @Mock
    private ReportHierarchyService reportHierarchyService;
    @Mock
    private OrganizationScopeResolver organizationScopeResolver;
    @Mock
    private AirAggregateRepository airAggregateRepository;
    @Mock
    private AirHealthCaseRepository caseRepository;
    @Mock
    private SystemSettingResolver settings;
    @Mock
    private ReferenceLookupService referenceLookupService;
    @Mock
    private PopulationLookupService populationLookupService;

    private AirHealthReportQueryService service;

    @BeforeEach
    void setUp() {
        lenient().when(settings.resolveString(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        service = new AirHealthReportQueryService(
                reportHierarchyService, new ReportDateRangeResolver(), organizationScopeResolver,
                airAggregateRepository, caseRepository, new AirQualityNorms(settings),
                referenceLookupService, populationLookupService);

        CurrentOrganizationContext.set(Organization.builder().build());
        lenient().when(reportHierarchyService.resolveNode(any(), any(), any()))
                .thenReturn(new ResolvedReportNode("UZ", "Republic", List.of(1L, 2L)));
        lenient().when(organizationScopeResolver.resolve(any())).thenReturn(republicScope());
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    private static ResolvedOrganizationScope republicScope() {
        return new ResolvedOrganizationScope(OrganizationScopeMode.ALL, null, null, null, null, null, null);
    }

    private static AirBucketStat air(LocalDate day, double average) {
        return new AirBucketStat(day, average, average + 5, 24);
    }

    @Test
    void seriesPairsAirWithCasesAndReportsTheirCorrelation() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        when(airAggregateRepository.bucketSeries(any(), any(), eq("PM2.5"), eq("day"), any(), any())).thenReturn(List.of(
                air(start, 10), air(start.plusDays(1), 20), air(start.plusDays(2), 30),
                air(start.plusDays(3), 40), air(start.plusDays(4), 50)));
        when(caseRepository.countByBucket(anyList(), eq(AirHealthBucket.DAY), eq(0), eq("J"), any(), any())).thenReturn(List.of(
                new BucketCases(start, 1), new BucketCases(start.plusDays(1), 2), new BucketCases(start.plusDays(2), 3),
                new BucketCases(start.plusDays(3), 4), new BucketCases(start.plusDays(4), 5)));

        AirHealthSeriesResponse response = service.getSeries(
                null, null, null, null, null, null, start, start.plusDays(4));

        assertThat(response.parameter()).isEqualTo("PM2.5");
        assertThat(response.norm()).isEqualByComparingTo("15");
        assertThat(response.points()).hasSize(5);
        assertThat(response.points().get(0).airAverage()).isEqualTo(10.0);
        assertThat(response.points().get(0).aboveNorm()).isFalse();
        assertThat(response.points().get(1).aboveNorm()).isTrue();
        assertThat(response.points().get(4).cases()).isEqualTo(5);
        assertThat(response.correlation().coefficient()).isEqualTo(1.0);
        assertThat(response.correlation().pairs()).isEqualTo(5);
        // Five points are arithmetic, not evidence.
        assertThat(response.correlation().reliable()).isFalse();
    }

    @Test
    void bucketsWithoutAirReadingsStayInTheSeriesButOutOfTheCorrelation() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        when(airAggregateRepository.bucketSeries(any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                air(start, 10), air(start.plusDays(2), 30), air(start.plusDays(3), 20)));
        when(caseRepository.countByBucket(anyList(), any(), anyInt(), any(), any(), any())).thenReturn(List.of(
                new BucketCases(start, 1), new BucketCases(start.plusDays(1), 99), new BucketCases(start.plusDays(2), 3),
                new BucketCases(start.plusDays(3), 2)));

        AirHealthSeriesResponse response = service.getSeries(
                null, null, null, null, null, null, start, start.plusDays(3));

        assertThat(response.points()).hasSize(4);
        assertThat(response.points().get(1).airAverage()).isNull();
        assertThat(response.points().get(1).cases()).isEqualTo(99);
        // The 99-case day has no air reading, so it must not drag the correlation down.
        assertThat(response.correlation().pairs()).isEqualTo(3);
        assertThat(response.correlation().coefficient()).isGreaterThan(0.9);
    }

    @Test
    void lagShiftsTheCaseWindowAndIsClamped() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 10);

        AirHealthSeriesResponse response = service.getSeries(
                null, null, null, null, AirHealthBucket.DAY, 99, start, end);

        assertThat(response.lagDays()).isEqualTo(AirHealthReportQueryService.MAX_LAG_DAYS);
        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(caseRepository).countByBucket(
                anyList(), eq(AirHealthBucket.DAY), eq(30), eq("J"), from.capture(), to.capture());
        assertThat(from.getValue()).isEqualTo(start.plusDays(30).atStartOfDay(ZONE).toInstant());
        assertThat(to.getValue()).isEqualTo(end.plusDays(31).atStartOfDay(ZONE).toInstant());
    }

    @Test
    void weeklySeriesStartsOnMondays() {
        // 2026-11-04 is a Wednesday; its week starts on Monday 2026-11-02.
        AirHealthSeriesResponse response = service.getSeries(
                null, null, null, null, AirHealthBucket.WEEK, null,
                LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 18));

        assertThat(response.points()).extracting(AirHealthSeriesResponse.Point::bucketStart)
                .containsExactly(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 16));
    }

    @Test
    void rejectsAReversedRangeAndAnOversizedSeries() {
        assertThatThrownBy(() -> service.getSeries(
                null, null, null, null, null, null, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1)))
                .isInstanceOf(AirHealthRequestException.class)
                .hasMessage("report.air-health.invalid-range");

        assertThatThrownBy(() -> service.getSeries(
                null, null, null, null, AirHealthBucket.DAY, null, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(AirHealthRequestException.class)
                .hasMessage("report.air-health.too-many-buckets");
    }

    @Test
    void territoriesJoinAirAndCasesRankByCasesAndLeaveTheRateOutWhenPopulationIsMissing() {
        when(airAggregateRepository.territoryStats(anyBoolean(), any(), any(), eq("PM2.5"), any(), any(), any()))
                .thenReturn(List.of(new AirTerritoryStat("1726", 3, 100, 20.0, 50.0, 4, 10)));
        when(caseRepository.countByTerritory(anyList(), anyBoolean(), eq("J"), any(), any())).thenReturn(List.of(
                new TerritoryCases("1727", 10), new TerritoryCases("1726", 50)));
        when(referenceLookupService.getRegionName("1726")).thenReturn("Toshkent");
        when(referenceLookupService.getRegionName("1727")).thenReturn("Samarqand");
        when(populationLookupService.resolveByNodeCode(eq("1726"), anyInt())).thenReturn(2_000_000L);
        when(populationLookupService.resolveByNodeCode(eq("1727"), anyInt())).thenReturn(0L);

        AirHealthTerritoriesResponse response = service.getTerritories(
                null, null, null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 1), null);

        verify(airAggregateRepository).territoryStats(eq(false), eq(null), eq(null), eq("PM2.5"),
                any(LocalDateTime.class), any(LocalDateTime.class), any());
        assertThat(response.rows()).extracting(r -> r.code()).containsExactly("1726", "1727");

        var toshkent = response.rows().get(0);
        assertThat(toshkent.confirmedCases()).isEqualTo(50);
        assertThat(toshkent.average()).isEqualTo(20.0);
        assertThat(toshkent.status()).isEqualTo(AirStatus.EXCEEDED);
        assertThat(toshkent.exceedanceDays()).isEqualTo(4);
        assertThat(toshkent.casesPerPopulation()).isEqualByComparingTo("2.50");

        var samarqand = response.rows().get(1);
        assertThat(samarqand.confirmedCases()).isEqualTo(10);
        assertThat(samarqand.average()).isNull();
        assertThat(samarqand.status()).isEqualTo(AirStatus.NO_DATA);
        assertThat(samarqand.casesPerPopulation()).isNull();

        assertThat(response.total().confirmedCases()).isEqualTo(60);
        assertThat(response.total().population()).isEqualTo(2_000_000L);
        // 1727 has cases but no population figure, so a republic rate would be inflated.
        assertThat(response.total().casesPerPopulation()).isNull();
        assertThat(response.koef()).isEqualTo(100_000);
    }

    @Test
    void aRegionScopedCallerSeesDistrictsInsteadOfRegions() {
        when(organizationScopeResolver.resolve(any())).thenReturn(
                new ResolvedOrganizationScope(OrganizationScopeMode.REGION, null, null, null, null, "1726", null));

        service.getTerritories(null, null, null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 1), null);

        verify(airAggregateRepository).territoryStats(eq(true), eq("1726"), eq(null), eq("PM2.5"),
                any(LocalDateTime.class), any(LocalDateTime.class), any());
        verify(caseRepository).countByTerritory(anyList(), eq(true), eq("J"), any(), any());
    }
}
