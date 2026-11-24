package uz.uzinfocom.app.orchestration.dashboard.application.query;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationResponse;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationStatusResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.AirQualityDashboardResponse;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilter;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilterResolver;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirQualityDashboardQueryServiceTest {

    private static AirStationStatusResponse station(long id, AirStatus status, LocalDateTime measuredAt) {
        AirStationResponse base = new AirStationResponse(id, "ext" + id, "S" + id, null, null, null, "Toshkent shahri", null, true, measuredAt);
        return new AirStationStatusResponse(base, measuredAt, status, null, null, List.of());
    }

    @Test
    void summarizesStationsByStatus() {
        LocalDateTime older = LocalDateTime.of(2026, 11, 16, 8, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 11, 16, 9, 0);

        AirQualityDashboardResponse.Summary summary = AirQualityDashboardQueryService.summarize(List.of(
                station(1, AirStatus.SEVERE, newer),
                station(2, AirStatus.EXCEEDED, older),
                station(3, AirStatus.GOOD, older),
                station(4, AirStatus.NO_DATA, null)));

        assertThat(summary.totalStations()).isEqualTo(4);
        assertThat(summary.stationsWithData()).isEqualTo(3);
        assertThat(summary.stationsAboveNorm()).isEqualTo(2);
        assertThat(summary.worstStatus()).isEqualTo(AirStatus.SEVERE);
        assertThat(summary.latestMeasuredAt()).isEqualTo(newer);
    }

    @Test
    void anEmptyScopeIsNoData() {
        AirQualityDashboardResponse.Summary summary = AirQualityDashboardQueryService.summarize(List.of());

        assertThat(summary.totalStations()).isZero();
        assertThat(summary.worstStatus()).isEqualTo(AirStatus.NO_DATA);
        assertThat(summary.latestMeasuredAt()).isNull();
    }

    @Test
    void theCallersScopeBecomesTheStationFilter() {
        assertThat(filter(OrganizationScopeMode.ALL, null, null)).isEqualTo(ScopeGeoFilter.NONE);
        assertThat(filter(OrganizationScopeMode.REGION, "1726", null)).isEqualTo(new ScopeGeoFilter("1726", null));
        assertThat(filter(OrganizationScopeMode.DISTRICT, "1726", "1726266")).isEqualTo(new ScopeGeoFilter("1726", "1726266"));
        // A single clinic is shown the stations of its own district, or its region when it has no district code.
        assertThat(filter(OrganizationScopeMode.ORGANIZATION, "1726", "1726266")).isEqualTo(new ScopeGeoFilter("1726", "1726266"));
        assertThat(filter(OrganizationScopeMode.ORGANIZATION, "1726", null)).isEqualTo(new ScopeGeoFilter("1726", null));
        assertThat(filter(OrganizationScopeMode.ORGANIZATION, null, null)).isEqualTo(ScopeGeoFilter.NONE);
    }

    private static ScopeGeoFilter filter(OrganizationScopeMode mode, String region, String district) {
        return ScopeGeoFilterResolver.of(new ResolvedOrganizationScope(mode, 1L, null, null, null, region, district));
    }
}
