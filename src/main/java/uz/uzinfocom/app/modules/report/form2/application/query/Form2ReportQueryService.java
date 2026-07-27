package uz.uzinfocom.app.modules.report.form2.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form2.application.query.dto.Form2OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form2.application.query.dto.Form2ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form2.infrastructure.persistence.repository.Form2ReportRepository;
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
 * "Form 2" — query service for the social/occupation composition report
 * (form058 + form058_1 combined, primary notifications only). Republic→
 * region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (shared with "Form 1" and "Form 3"); this
 * class only supplies the counting strategy (implements {@link
 * ReportCountSource}, backed by {@link Form2ReportRepository}'s native SQL,
 * all counting done in the database) and maps the generic {@link
 * ReportHierarchyNode} into this report's own response shape. Holds no
 * per-case data in memory — only the already-aggregated projection per
 * geography node.
 */
@Service
@RequiredArgsConstructor
public class Form2ReportQueryService implements ReportCountSource<Form2OrganizationCountProjection> {

    private final Form2ReportRepository form2ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<Form2ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);

        return reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode)
                .stream()
                .map(this::toNode)
                .toList();
    }

    public List<Form2ReportNodeResponse> getChildren(
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
    public Form2OrganizationCountProjection total(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form2ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form2OrganizationCountProjection> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form2ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(Form2OrganizationCountProjection::organizationId, row -> row));
    }

    @Override
    public Form2OrganizationCountProjection empty() {
        return Form2OrganizationCountProjection.empty(null);
    }

    @Override
    public Form2OrganizationCountProjection merge(
            Form2OrganizationCountProjection a, Form2OrganizationCountProjection b
    ) {
        return Form2OrganizationCountProjection.add(a, b);
    }

    private Form2ReportNodeResponse toNode(ReportHierarchyNode<Form2OrganizationCountProjection> node) {
        Form2OrganizationCountProjection counts = node.counts();
        return new Form2ReportNodeResponse(
                node.code(),
                node.name(),
                node.hasChildren(),
                counts.total(),
                counts.unorganizedPreschool(),
                counts.organizedPreschool(),
                counts.schoolStudents(),
                counts.vocationalStudents(),
                counts.universityStudents(),
                counts.employees(),
                counts.workers(),
                counts.medicalStaff(),
                counts.unemployed()
        );
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
