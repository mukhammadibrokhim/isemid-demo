package uz.uzinfocom.app.modules.report.form6.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeBreakdownProjection;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeBreakdownResponse;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6AgeGroupRowResponse;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form6.application.query.dto.Form6ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form6.infrastructure.persistence.repository.Form6ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * "Form 6" — query service for the infectious/parasitic disease age-structure
 * report (form058 + form058_1 combined, primary/not-yet-decided notifications
 * only). Republic→region→district→organization drill-down is delegated to
 * {@link ReportHierarchyService} (implements {@link ReportCountSource} with
 * a plain {@link Long} total, backed by {@link Form6ReportRepository}), same
 * as "Form 1"/"Form 4" — the twist specific to this report is that every
 * node is shown twice, once for the caller's period and once for the same
 * calendar dates one year earlier ({@link ReportDateRangeResolver}'s {@code
 * yearsAgo} parameter exists exactly for this), zipped together by node
 * code into a single response row with the delta. The age-group breakdown
 * (triggered from a specific row, not part of the geography drill-down) is
 * a separate query over that node's whole organization subtree, resolved via
 * {@link ReportHierarchyService#resolveNode}.
 */
@Service
@RequiredArgsConstructor
public class Form6ReportQueryService implements ReportCountSource<Long> {

    private record AgeGroupSpec(String code, String messageKey, ToLongFunction<Form6AgeBreakdownProjection> accessor) {
    }

    /** Display order matches the reference screenshot: Jami first, then youngest to oldest. */
    private static final List<AgeGroupSpec> AGE_GROUPS = List.of(
            new AgeGroupSpec("TOTAL", "report.scope.total", Form6AgeBreakdownProjection::total),
            new AgeGroupSpec("NEWBORN", "report.form6.age.newborn", Form6AgeBreakdownProjection::newborn),
            new AgeGroupSpec("AGE_1_2", "report.form6.age.age1to2", Form6AgeBreakdownProjection::age1to2),
            new AgeGroupSpec("AGE_3_5", "report.form6.age.age3to5", Form6AgeBreakdownProjection::age3to5),
            new AgeGroupSpec("AGE_6_14", "report.form6.age.age6to14", Form6AgeBreakdownProjection::age6to14),
            new AgeGroupSpec("AGE_15_17", "report.form6.age.age15to17", Form6AgeBreakdownProjection::age15to17),
            new AgeGroupSpec("UNDER_18", "report.form6.age.under18", Form6AgeBreakdownProjection::under18),
            new AgeGroupSpec("AGE_19_25", "report.form6.age.age19to25", Form6AgeBreakdownProjection::age19to25),
            new AgeGroupSpec("AGE_26_40", "report.form6.age.age26to40", Form6AgeBreakdownProjection::age26to40),
            new AgeGroupSpec("AGE_41_55", "report.form6.age.age41to55", Form6AgeBreakdownProjection::age41to55),
            new AgeGroupSpec("AGE_56_70", "report.form6.age.age56to70", Form6AgeBreakdownProjection::age56to70),
            new AgeGroupSpec("OVER_70", "report.form6.age.over70", Form6AgeBreakdownProjection::over70)
    );

    private final Form6ReportRepository form6ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final MessageResolver messageResolver;

    public List<Form6ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Long>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Long>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes);
    }

    public List<Form6ReportNodeResponse> getChildren(
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

    public Form6AgeBreakdownResponse getAgeBreakdown(
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

        Form6AgeBreakdownProjection currentCounts = form6ReportRepository.countAgeBreakdown(
                node.organizationIds(), currentRange.fromInclusive(), currentRange.toExclusive(), diagnosisCode
        );
        Form6AgeBreakdownProjection previousCounts = form6ReportRepository.countAgeBreakdown(
                node.organizationIds(), previousRange.fromInclusive(), previousRange.toExclusive(), diagnosisCode
        );

        return new Form6AgeBreakdownResponse(node.code(), node.name(), zipAgeGroups(currentCounts, previousCounts));
    }

    @Override
    public Long total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form6ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Long> groupedByOrganization(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form6ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(Form6OrganizationCountProjection::organizationId, Form6OrganizationCountProjection::total));
    }

    @Override
    public Long empty() {
        return 0L;
    }

    @Override
    public Long merge(Long a, Long b) {
        return a + b;
    }

    private List<Form6ReportNodeResponse> zip(
            List<ReportHierarchyNode<Long>> currentNodes, List<ReportHierarchyNode<Long>> previousNodes
    ) {
        Map<String, Long> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> {
                    long currentYear = node.counts();
                    long previousYear = previousByCode.getOrDefault(node.code(), 0L);
                    return new Form6ReportNodeResponse(
                            node.code(), node.name(), node.hasChildren(), previousYear, currentYear, currentYear - previousYear
                    );
                })
                .toList();
    }

    private List<Form6AgeGroupRowResponse> zipAgeGroups(
            Form6AgeBreakdownProjection current, Form6AgeBreakdownProjection previous
    ) {
        return AGE_GROUPS.stream()
                .map(group -> {
                    long currentYear = group.accessor().applyAsLong(current);
                    long previousYear = group.accessor().applyAsLong(previous);
                    return new Form6AgeGroupRowResponse(
                            group.code(), messageResolver.resolve(group.messageKey()),
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
