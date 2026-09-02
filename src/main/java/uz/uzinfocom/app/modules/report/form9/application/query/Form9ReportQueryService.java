package uz.uzinfocom.app.modules.report.form9.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9Counts;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthRowResponse;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthlyBreakdownResponse;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9MonthlyCountProjection;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form9.application.query.dto.Form9ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form9.infrastructure.persistence.repository.Form9ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Form 9" — query service for the comparative infectious-disease report
 * ({@code form058} + {@code form058_1} combined, primary/not-yet-decided
 * notifications only). Structurally identical to "Form 6" / "Form 8":
 * republic→region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (implements {@link ReportCountSource}, backed by
 * {@link Form9ReportRepository}), and every node is shown twice — once for
 * the caller's period, once for the same calendar dates one year earlier
 * ({@link ReportDateRangeResolver}'s {@code yearsAgo} parameter) — zipped by
 * node code into one response row with the delta.
 * <p>
 * Two twists relative to "Form 8": the count aggregate is a two-metric
 * {@link Form9Counts} (registered + hospitalized) rather than a plain {@link
 * Long}, and the per-node breakdown (triggered from a specific row, not part
 * of the geography drill-down) is by <b>calendar month</b> (12 fixed rows +
 * "Jami") rather than by social category.
 */
@Service
@RequiredArgsConstructor
public class Form9ReportQueryService implements ReportCountSource<Form9Counts> {

    private static final int MONTHS_IN_YEAR = 12;
    private static final String TOTAL_ROW_CODE = "TOTAL";

    private final Form9ReportRepository form9ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final MessageResolver messageResolver;

    public List<Form9ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Form9Counts>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Form9Counts>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes);
    }

    public List<Form9ReportNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Form9Counts>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Form9Counts>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes);
    }

    public Form9MonthlyBreakdownResponse getMonthlyBreakdown(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ResolvedReportNode node = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode);

        ReportDateRange currentRange = reportDateRangeResolver.resolve(from, to);
        ReportDateRange previousRange = reportDateRangeResolver.resolve(from, to, 1);

        Map<Integer, Form9Counts> currentByMonth = indexByMonth(form9ReportRepository.countMonthlyBreakdown(
                node.organizationIds(), currentRange.fromInclusive(), currentRange.toExclusive(), diagnosisCode
        ));
        Map<Integer, Form9Counts> previousByMonth = indexByMonth(form9ReportRepository.countMonthlyBreakdown(
                node.organizationIds(), previousRange.fromInclusive(), previousRange.toExclusive(), diagnosisCode
        ));

        List<Form9MonthRowResponse> rows = new ArrayList<>(MONTHS_IN_YEAR + 1);
        Form9Counts currentTotal = Form9Counts.EMPTY;
        Form9Counts previousTotal = Form9Counts.EMPTY;

        for (int month = 1; month <= MONTHS_IN_YEAR; month++) {
            Form9Counts current = currentByMonth.getOrDefault(month, Form9Counts.EMPTY);
            Form9Counts previous = previousByMonth.getOrDefault(month, Form9Counts.EMPTY);
            currentTotal = currentTotal.plus(current);
            previousTotal = previousTotal.plus(previous);
            rows.add(row(String.valueOf(month), messageResolver.resolve("report.form9.month." + month), previous, current));
        }
        rows.add(row(TOTAL_ROW_CODE, messageResolver.resolve("report.scope.total"), previousTotal, currentTotal));

        return new Form9MonthlyBreakdownResponse(node.code(), node.name(), rows);
    }

    @Override
    public Form9Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form9ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form9Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form9ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(
                        Form9OrganizationCountProjection::organizationId,
                        p -> new Form9Counts(p.registered(), p.hospitalized())
                ));
    }

    @Override
    public Form9Counts empty() {
        return Form9Counts.EMPTY;
    }

    @Override
    public Form9Counts merge(Form9Counts a, Form9Counts b) {
        return a.plus(b);
    }

    private List<Form9ReportNodeResponse> zip(
            List<ReportHierarchyNode<Form9Counts>> currentNodes, List<ReportHierarchyNode<Form9Counts>> previousNodes
    ) {
        Map<String, Form9Counts> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> {
                    Form9Counts current = node.counts();
                    Form9Counts previous = previousByCode.getOrDefault(node.code(), Form9Counts.EMPTY);
                    return new Form9ReportNodeResponse(
                            node.code(), node.name(), node.hasChildren(),
                            previous.registered(), current.registered(),
                            current.registered() - previous.registered(),
                            previous.hospitalized(), current.hospitalized(),
                            current.hospitalized() - previous.hospitalized()
                    );
                })
                .toList();
    }

    private Form9MonthRowResponse row(String code, String name, Form9Counts previous, Form9Counts current) {
        return new Form9MonthRowResponse(
                code, name,
                previous.registered(), current.registered(), current.registered() - previous.registered(),
                previous.hospitalized(), current.hospitalized(), current.hospitalized() - previous.hospitalized()
        );
    }

    private Map<Integer, Form9Counts> indexByMonth(List<Form9MonthlyCountProjection> projections) {
        return projections.stream()
                .collect(Collectors.toMap(
                        Form9MonthlyCountProjection::month,
                        p -> new Form9Counts(p.registered(), p.hospitalized())
                ));
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
