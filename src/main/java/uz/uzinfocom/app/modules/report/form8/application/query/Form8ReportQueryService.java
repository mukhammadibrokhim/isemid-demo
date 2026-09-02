package uz.uzinfocom.app.modules.report.form8.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8CategoryBreakdownProjection;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8CategoryBreakdownResponse;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8CategoryRowResponse;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form8.application.query.dto.Form8ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form8.infrastructure.persistence.repository.Form8ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * "Form 8" — query service for the "infectious/parasitic disease by social
 * composition" comparison report (form058 + form058_1 combined, primary/
 * not-yet-decided notifications only). Structurally identical to "Form 6":
 * republic→region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (implements {@link ReportCountSource} with a plain
 * {@link Long} total, backed by {@link Form8ReportRepository}), and every
 * node is shown twice — once for the caller's period, once for the same
 * calendar dates one year earlier ({@link ReportDateRangeResolver}'s {@code
 * yearsAgo} parameter) — zipped by node code into one response row with the
 * delta. The twist specific to this report is that the per-node breakdown
 * (triggered from a specific row, not part of the geography drill-down) is by
 * social category ({@code patient.category_code}, the same set "Form 4"
 * breaks out) rather than by age group.
 */
@Service
@RequiredArgsConstructor
public class Form8ReportQueryService implements ReportCountSource<Long> {

    private record CategorySpec(
            String code, String messageKey, ToLongFunction<Form8CategoryBreakdownProjection> accessor
    ) {
    }

    /** Display order matches the reference screenshot: Jami first, then the 11 broken-out categories. */
    private static final List<CategorySpec> CATEGORIES = List.of(
            new CategorySpec("TOTAL", "report.scope.total", Form8CategoryBreakdownProjection::total),
            new CategorySpec("NO_ORGANIZED", "report.form8.category.unorganizedPreschool",
                    Form8CategoryBreakdownProjection::unorganizedPreschool),
            new CategorySpec("ORGANIZED", "report.form8.category.organizedPreschool",
                    Form8CategoryBreakdownProjection::organizedPreschool),
            new CategorySpec("WORKER", "report.form8.category.workers",
                    Form8CategoryBreakdownProjection::workers),
            new CategorySpec("NOT_EMPLOYED", "report.form8.category.unemployed",
                    Form8CategoryBreakdownProjection::unemployed),
            new CategorySpec("PENSIONER", "report.form8.category.pensioners",
                    Form8CategoryBreakdownProjection::pensioners),
            new CategorySpec("STUDENT_SCHOOL", "report.form8.category.schoolStudents",
                    Form8CategoryBreakdownProjection::schoolStudents),
            new CategorySpec("UNSHELTRED", "report.form8.category.unsheltered",
                    Form8CategoryBreakdownProjection::unsheltered),
            new CategorySpec("SEREVANTS", "report.form8.category.employees",
                    Form8CategoryBreakdownProjection::employees),
            new CategorySpec("MEDICAL_WORKER", "report.form8.category.medicalStaff",
                    Form8CategoryBreakdownProjection::medicalStaff),
            new CategorySpec("MIDDLE_STUDENT", "report.form8.category.vocationalStudents",
                    Form8CategoryBreakdownProjection::vocationalStudents),
            new CategorySpec("STUDENT", "report.form8.category.universityStudents",
                    Form8CategoryBreakdownProjection::universityStudents)
    );

    private final Form8ReportRepository form8ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final MessageResolver messageResolver;

    public List<Form8ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Long>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Long>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes);
    }

    public List<Form8ReportNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Long>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Long>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes);
    }

    public Form8CategoryBreakdownResponse getCategoryBreakdown(
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

        Form8CategoryBreakdownProjection currentCounts = form8ReportRepository.countCategoryBreakdown(
                node.organizationIds(), currentRange.fromInclusive(), currentRange.toExclusive(), diagnosisCode
        );
        Form8CategoryBreakdownProjection previousCounts = form8ReportRepository.countCategoryBreakdown(
                node.organizationIds(), previousRange.fromInclusive(), previousRange.toExclusive(), diagnosisCode
        );

        return new Form8CategoryBreakdownResponse(node.code(), node.name(), zipCategories(currentCounts, previousCounts));
    }

    @Override
    public Long total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form8ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Long> groupedByOrganization(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form8ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(
                        Form8OrganizationCountProjection::organizationId, Form8OrganizationCountProjection::total
                ));
    }

    @Override
    public Long empty() {
        return 0L;
    }

    @Override
    public Long merge(Long a, Long b) {
        return a + b;
    }

    private List<Form8ReportNodeResponse> zip(
            List<ReportHierarchyNode<Long>> currentNodes, List<ReportHierarchyNode<Long>> previousNodes
    ) {
        Map<String, Long> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> {
                    long currentYear = node.counts();
                    long previousYear = previousByCode.getOrDefault(node.code(), 0L);
                    return new Form8ReportNodeResponse(
                            node.code(), node.name(), node.hasChildren(), previousYear, currentYear, currentYear - previousYear
                    );
                })
                .toList();
    }

    private List<Form8CategoryRowResponse> zipCategories(
            Form8CategoryBreakdownProjection current, Form8CategoryBreakdownProjection previous
    ) {
        return CATEGORIES.stream()
                .map(category -> {
                    long currentYear = category.accessor().applyAsLong(current);
                    long previousYear = category.accessor().applyAsLong(previous);
                    return new Form8CategoryRowResponse(
                            category.code(), messageResolver.resolve(category.messageKey()),
                            previousYear, currentYear, currentYear - previousYear
                    );
                })
                .toList();
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
