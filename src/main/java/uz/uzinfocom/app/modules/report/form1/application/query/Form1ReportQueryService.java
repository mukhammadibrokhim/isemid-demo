package uz.uzinfocom.app.modules.report.form1.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1CountBlockResponse;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form1.application.query.dto.Form1ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form1.infrastructure.persistence.repository.Form1ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Form 1" — query service for the infectious/parasitic disease monitoring
 * report (form058 + form058_1 combined), for an arbitrary caller-supplied
 * {@code [from, to]} date range. Republic→region→district→organization
 * drill-down is delegated to {@link ReportHierarchyService} (shared with
 * "Form 2" and "Form 3"); this class only supplies the counting strategy
 * (implements {@link ReportCountSource}, backed by {@link
 * Form1ReportRepository}'s native SQL, grouped by the case's <b>creating</b>
 * organization) and maps the generic {@link ReportHierarchyNode} back into
 * this report's own response shape.
 */
@Service
@RequiredArgsConstructor
public class Form1ReportQueryService implements ReportCountSource<Form1OrganizationCountProjection> {

    private final Form1ReportRepository form1ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<Form1ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);

        return reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode)
                .stream()
                .map(this::toNode)
                .toList();
    }

    public List<Form1ReportNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);

        return reportHierarchyService
                .loadChildren(currentOrganization, regionCode, districtCode, this, range, diagnosisCode)
                .stream()
                .map(this::toNode)
                .toList();
    }

    @Override
    public Form1OrganizationCountProjection total(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form1ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form1OrganizationCountProjection> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form1ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(Form1OrganizationCountProjection::organizationId, row -> row));
    }

    @Override
    public Form1OrganizationCountProjection empty() {
        return Form1OrganizationCountProjection.empty(null);
    }

    @Override
    public Form1OrganizationCountProjection merge(
            Form1OrganizationCountProjection a, Form1OrganizationCountProjection b
    ) {
        return Form1OrganizationCountProjection.add(a, b);
    }

    private Form1ReportNodeResponse toNode(ReportHierarchyNode<Form1OrganizationCountProjection> node) {
        Form1OrganizationCountProjection counts = node.counts();
        return new Form1ReportNodeResponse(
                node.code(),
                node.name(),
                node.hasChildren(),
                new Form1CountBlockResponse(
                        counts.confirmedTotal(), counts.confirmedUnder14(), counts.confirmedUnder18(),
                        counts.confirmedAdult(), counts.confirmedFemale()
                ),
                diagnosisChangePercent(counts),
                new Form1CountBlockResponse(
                        counts.primaryTotal(), counts.primaryUnder14(), counts.primaryUnder18(),
                        counts.primaryAdult(), counts.primaryFemale()
                )
        );
    }

    private double diagnosisChangePercent(Form1OrganizationCountProjection counts) {
        if (counts.confirmedTotal() == 0) {
            return 0.0;
        }
        return Math.round((counts.confirmedDiagnosisChanged() * 10000.0) / counts.confirmedTotal()) / 100.0;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
