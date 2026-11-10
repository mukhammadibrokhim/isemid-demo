package uz.uzinfocom.app.modules.report.analytic.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportComputeRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportComputeResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportDiagnosisCount;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportRegionBreakdown;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.AnalyticReportCountRepository;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.dto.AnalyticReportDiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stateless "preview" the frontend calls on every filter change to feed the
 * "Analitik hisobot" editor auto-fill (the "Koeffitsiyent / <hudud> / Aholi
 * soni / <tashxis> / Tasdiqlangan / <nisbiy ko'rsatkich>" block the caller
 * then free-edits before saving). Nothing here is persisted — see {@code
 * AnalyticReportCommandService} for that.
 * <p>
 * Population is resolved from {@code ref_population} for the <b>current</b>
 * calendar year via {@link PopulationLookupService} — replacing the manually
 * typed "Aholi soni" figure the legacy flow used, which could silently drift
 * from the reference data.
 */
@Service
@RequiredArgsConstructor
public class AnalyticReportComputeService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final long DEFAULT_KOEF = 100_000L;

    private final AnalyticReportCountRepository analyticReportCountRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final PopulationLookupService populationLookupService;
    private final Icd10LookupService icd10LookupService;

    @Transactional(readOnly = true)
    public AnalyticReportComputeResponse compute(AnalyticReportComputeRequest request) {
        Organization currentOrganization = requireCurrentOrganization();
        long koef = request.koef() != null ? request.koef() : DEFAULT_KOEF;
        int year = LocalDate.now(APPLICATION_ZONE).getYear();

        Set<String> icd10Codes = normalize(request.icd10Codes());
        Map<String, String> diagnosisNames = icd10LookupService.resolveNames(icd10Codes);
        ReportDateRange range = reportDateRangeResolver.resolve(request.from(), request.to());

        List<AnalyticReportRegionBreakdown> regions = normalize(request.regionCodes()).stream()
                .map(regionCode -> regionBreakdown(
                        currentOrganization, regionCode, icd10Codes, diagnosisNames, range, koef, year
                ))
                .toList();

        return new AnalyticReportComputeResponse(koef, regions);
    }

    private AnalyticReportRegionBreakdown regionBreakdown(
            Organization currentOrganization,
            String regionCode,
            Set<String> icd10Codes,
            Map<String, String> diagnosisNames,
            ReportDateRange range,
            long koef,
            int year
    ) {
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, null);
        long population = populationLookupService.resolveByNodeCode(node.code(), year);

        Map<String, Long> countsByCode = analyticReportCountRepository
                .countGroupedByDiagnosisCodeForCodes(
                        node.organizationIds(), range.fromInclusive(), range.toExclusive(), icd10Codes
                )
                .stream()
                .collect(Collectors.toMap(
                        p -> p.diagnosisCode().toUpperCase(Locale.ROOT),
                        AnalyticReportDiagnosisCountProjection::count
                ));

        List<AnalyticReportDiagnosisCount> diagnoses = icd10Codes.stream()
                .map(code -> {
                    long count = countsByCode.getOrDefault(code, 0L);
                    double rate = population > 0 ? round2((double) count / population * koef) : 0.0;
                    return new AnalyticReportDiagnosisCount(code, diagnosisNames.getOrDefault(code, code), count, rate);
                })
                .toList();

        return new AnalyticReportRegionBreakdown(node.code(), node.name(), population, diagnoses);
    }

    private Set<String> normalize(Set<String> codes) {
        if (codes == null) {
            return Set.of();
        }
        return codes.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
