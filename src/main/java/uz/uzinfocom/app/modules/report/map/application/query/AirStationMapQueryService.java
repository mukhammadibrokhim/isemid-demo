package uz.uzinfocom.app.modules.report.map.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.airquality.application.query.AirQualityQueryService;
import uz.uzinfocom.app.modules.airquality.application.query.dto.AirStationStatusResponse;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilter;
import uz.uzinfocom.app.orchestration.scope.ScopeGeoFilterResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.List;

/**
 * Air-quality stations for the case map: the pins to draw next to the form058 points, each with
 * its latest reading judged against the norms. Territory scoping works as on the points
 * endpoint - an explicit {@code regionCode}/{@code districtCode} must be inside the caller's
 * access scope ({@link ReportHierarchyService#resolveNode} rejects it otherwise), and with
 * neither the caller's own scope is the default.
 */
@Service
@RequiredArgsConstructor
public class AirStationMapQueryService {

    private final AirQualityQueryService airQualityQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ScopeGeoFilterResolver scopeGeoFilterResolver;

    public List<AirStationStatusResponse> getStations(String regionCode, String districtCode, boolean activeOnly) {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));

        ScopeGeoFilter filter;
        if (StringUtils.hasText(regionCode) || StringUtils.hasText(districtCode)) {
            reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
            filter = new ScopeGeoFilter(blankToNull(regionCode), blankToNull(districtCode));
        } else {
            filter = scopeGeoFilterResolver.resolve(currentOrganization);
        }
        return airQualityQueryService.stationStatuses(filter.regionCode(), filter.districtCode(), activeOnly);
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
