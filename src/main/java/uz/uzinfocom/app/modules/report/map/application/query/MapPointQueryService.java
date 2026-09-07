package uz.uzinfocom.app.modules.report.map.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.map.application.query.dto.MapPointProjection;
import uz.uzinfocom.app.modules.report.map.application.query.dto.MapPointResponse;
import uz.uzinfocom.app.modules.report.map.infrastructure.persistence.repository.MapPointRepository;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * "Case map" — a flat, territory- and diagnosis-filtered list of form058
 * case points (coordinates from {@code fm058_location}) for frontend map
 * plotting. Unlike the drill-down reports under {@code modules.report},
 * this has no hierarchy: territory scoping reuses {@link
 * ReportHierarchyService#resolveNode} purely to turn an optional {@code
 * regionCode}/{@code districtCode} into the same scope-checked
 * organization-id list every other report resolves (rejecting a request
 * outside the caller's access scope the same way), then hands that
 * straight to {@link MapPointRepository} instead of walking it into a
 * breakdown.
 */
@Service
@RequiredArgsConstructor
public class MapPointQueryService {

    private static final String APPROVED_STATUS = "APPROVED";

    private final MapPointRepository mapPointRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<MapPointResponse> getPoints(
            String regionCode, String districtCode, String diagnosisCode, String status, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);

        List<MapPointProjection> points = mapPointRepository.findPoints(
                node.organizationIds(),
                range.fromInclusive(),
                range.toExclusive(),
                normalize(status),
                normalize(diagnosisCode)
        );

        return points.stream()
                .map(p -> new MapPointResponse(
                        p.id(), p.latitude(), p.longitude(), p.address(), p.diagnosisCode(),
                        APPROVED_STATUS.equals(p.status()), p.status(), p.createdAt()
                ))
                .toList();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
