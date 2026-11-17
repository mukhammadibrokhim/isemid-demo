package uz.uzinfocom.app.orchestration.dashboard.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;
import uz.uzinfocom.app.modules.airquality.application.query.AirQualityQueryService;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationStatusResponse;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.AirQualityDashboardResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilter;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilterResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * The air-quality card of the dashboard: stations inside the caller's territory with their
 * latest reading judged against the norms. Reads only stored data (no upstream call), so it
 * is as cheap as the other dashboard snapshots.
 */
@Service
@RequiredArgsConstructor
public class AirQualityDashboardQueryService {

    private final OrganizationScopeResolver organizationScopeResolver;
    private final AirQualityQueryService airQualityQueryService;
    private final ReferenceLookupService referenceLookupService;

    public AirQualityDashboardResponse getDashboard() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        ScopeGeoFilter filter = ScopeGeoFilterResolver.of(scope);

        List<AirStationStatusResponse> stations =
                airQualityQueryService.stationStatuses(filter.regionCode(), filter.districtCode(), true);
        return new AirQualityDashboardResponse(generatedAt, scopeResponse(scope), summarize(stations), stations);
    }

    static AirQualityDashboardResponse.Summary summarize(List<AirStationStatusResponse> stations) {
        int withData = (int) stations.stream().filter(s -> s.status() != AirStatus.NO_DATA).count();
        int aboveNorm = (int) stations.stream().filter(s -> s.status().isAboveNorm()).count();
        AirStatus worst = stations.stream()
                .map(AirStationStatusResponse::status)
                .max(Comparator.comparingInt(AirStatus::severity))
                .orElse(AirStatus.NO_DATA);
        LocalDateTime latest = stations.stream()
                .map(AirStationStatusResponse::measuredAt)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new AirQualityDashboardResponse.Summary(stations.size(), withData, aboveNorm, worst, latest);
    }

    private DashboardScopeResponse scopeResponse(ResolvedOrganizationScope scope) {
        String regionName = scope.regionCode() != null ? referenceLookupService.getRegionName(scope.regionCode()) : null;
        String districtName = scope.districtCode() != null
                ? referenceLookupService.getDistrictName(scope.districtCode())
                : null;
        return new DashboardScopeResponse(scope.mode(), scope.regionCode(), regionName, scope.districtCode(), districtName);
    }
}
