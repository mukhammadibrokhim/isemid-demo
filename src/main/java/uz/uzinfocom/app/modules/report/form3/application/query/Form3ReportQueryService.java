package uz.uzinfocom.app.modules.report.form3.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form3.application.query.dto.Form3ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form3.infrastructure.persistence.repository.Form3ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Form 3" — query service for the year-over-year comparative report
 * (form058 + form058_1 combined, primary notifications only). Republic→
 * region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (shared with "Form 1" and "Form 2"); the count
 * shape here is a plain {@code Long} (just a total, no age/gender/category
 * cut), so this class doubles as the {@link ReportCountSource
 * ReportCountSource&lt;Long&gt;} for both periods it needs.
 * <p>
 * Each of {@link #getRoot} / {@link #getChildren} walks the hierarchy
 * <b>twice</b> — once for the caller's {@code [from, to]}, once for the same
 * calendar dates one year earlier — and zips the two node lists by {@code
 * code} (both walks visit identical geography, so every current-period node
 * has a matching previous-period node, defaulting to zero if a region/
 * district had no organizations a year ago). Two independent walks, rather
 * than teaching {@link ReportHierarchyService} to batch two periods at once,
 * keeps the shared engine's contract simple — each walk is already a single
 * bounded aggregate query over at most ~1000 organizations countrywide, and
 * the only thing zipped in Java is a pair of small (already-aggregated)
 * per-node totals, never raw case rows.
 */
@Service
@RequiredArgsConstructor
public class Form3ReportQueryService implements ReportCountSource<Long> {

    private final Form3ReportRepository form3ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<Form3ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange currentRange = reportDateRangeResolver.resolve(from, to, 0);
        ReportDateRange previousRange = reportDateRangeResolver.resolve(from, to, 1);

        List<ReportHierarchyNode<Long>> currentNodes =
                reportHierarchyService.loadRootBreakdown(currentOrganization, this, currentRange, diagnosisCode);
        List<ReportHierarchyNode<Long>> previousNodes =
                reportHierarchyService.loadRootBreakdown(currentOrganization, this, previousRange, diagnosisCode);

        Map<String, Long> previousTotalByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> toNode(node, previousTotalByCode.getOrDefault(node.code(), 0L)))
                .toList();
    }

    public List<Form3ReportNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange currentRange = reportDateRangeResolver.resolve(from, to, 0);
        ReportDateRange previousRange = reportDateRangeResolver.resolve(from, to, 1);

        List<ReportHierarchyNode<Long>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this, currentRange, diagnosisCode
        );
        List<ReportHierarchyNode<Long>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this, previousRange, diagnosisCode
        );

        Map<String, Long> previousTotalByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> toNode(node, previousTotalByCode.getOrDefault(node.code(), 0L)))
                .toList();
    }

    @Override
    public Long total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form3ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Long> groupedByOrganization(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form3ReportRepository.countGroupedByOrganization(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Long empty() {
        return 0L;
    }

    @Override
    public Long merge(Long a, Long b) {
        return a + b;
    }

    private Form3ReportNodeResponse toNode(ReportHierarchyNode<Long> currentNode, long previousYearTotal) {
        long currentYearTotal = currentNode.counts();
        return new Form3ReportNodeResponse(
                currentNode.code(),
                currentNode.name(),
                currentNode.hasChildren(),
                previousYearTotal,
                currentYearTotal,
                currentYearTotal - previousYearTotal
        );
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
