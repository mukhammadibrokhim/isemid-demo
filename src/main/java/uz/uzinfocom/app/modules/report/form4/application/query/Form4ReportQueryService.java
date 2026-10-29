package uz.uzinfocom.app.modules.report.form4.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.report.form4.application.query.dto.Form4CategoryCountBlockResponse;
import uz.uzinfocom.app.modules.report.form4.application.query.dto.Form4OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form4.application.query.dto.Form4ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form4.infrastructure.persistence.repository.Form4ReportRepository;
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
 * "Form 4" — query service for the social/occupation composition report
 * (form058 + form058_1 combined, confirmed/primary split, counts bucketed by
 * {@code patient.category_code}). Republic→region→district→organization
 * drill-down is delegated to {@link ReportHierarchyService} (shared with
 * "Form 1"); this class only supplies the counting strategy (implements
 * {@link ReportCountSource}, backed by {@link Form4ReportRepository}'s
 * native SQL) and maps the generic {@link ReportHierarchyNode} back into
 * this report's own response shape.
 */
@Service
@RequiredArgsConstructor
public class Form4ReportQueryService implements ReportCountSource<Form4OrganizationCountProjection> {

    private final Form4ReportRepository form4ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<Form4ReportNodeResponse> getRoot(LocalDate from, LocalDate to, String diagnosisCode) {
        Organization currentOrganization = requireCurrentOrganization();
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);

        return reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode)
                .stream()
                .map(this::toNode)
                .toList();
    }

    public List<Form4ReportNodeResponse> getChildren(
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
    public Form4OrganizationCountProjection total(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form4ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form4OrganizationCountProjection> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form4ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(Form4OrganizationCountProjection::organizationId, row -> row));
    }

    @Override
    public Form4OrganizationCountProjection empty() {
        return Form4OrganizationCountProjection.empty(null);
    }

    @Override
    public Form4OrganizationCountProjection merge(
            Form4OrganizationCountProjection a, Form4OrganizationCountProjection b
    ) {
        return Form4OrganizationCountProjection.add(a, b);
    }

    private Form4ReportNodeResponse toNode(ReportHierarchyNode<Form4OrganizationCountProjection> node) {
        Form4OrganizationCountProjection counts = node.counts();
        return new Form4ReportNodeResponse(
                node.code(),
                node.name(),
                node.hasChildren(),
                new Form4CategoryCountBlockResponse(
                        counts.confirmedTotal(),
                        counts.confirmedUnorganizedPreschool(),
                        counts.confirmedOrganizedPreschool(),
                        counts.confirmedSchoolStudents(),
                        counts.confirmedVocationalStudents(),
                        counts.confirmedUniversityStudents(),
                        counts.confirmedEmployees(),
                        counts.confirmedWorkers(),
                        counts.confirmedMedicalStaff(),
                        counts.confirmedUnemployed(),
                        counts.confirmedPensioners(),
                        counts.confirmedUnsheltered()
                ),
                new Form4CategoryCountBlockResponse(
                        counts.primaryTotal(),
                        counts.primaryUnorganizedPreschool(),
                        counts.primaryOrganizedPreschool(),
                        counts.primarySchoolStudents(),
                        counts.primaryVocationalStudents(),
                        counts.primaryUniversityStudents(),
                        counts.primaryEmployees(),
                        counts.primaryWorkers(),
                        counts.primaryMedicalStaff(),
                        counts.primaryUnemployed(),
                        counts.primaryPensioners(),
                        counts.primaryUnsheltered()
                )
        );
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
