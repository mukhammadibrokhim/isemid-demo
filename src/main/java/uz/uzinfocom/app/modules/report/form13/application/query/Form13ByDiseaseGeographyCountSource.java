package uz.uzinfocom.app.modules.report.form13.application.query;

import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository.Form13ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link ReportCountSource} strategy for a single "Form 13 by disease"
 * geography drill-down — structurally identical to {@code
 * Form12GeographyCountSource}: it counts confirmed cases whose diagnosis is
 * in one {@code FORM_13} catalog entry's ICD-10 code set, bucketed
 * region→district→organization by the shared {@code ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — it is constructed per request in
 * {@code Form13ByDiseaseReportQueryService} with the code set of the catalog
 * entry being expanded, then handed to {@code
 * ReportHierarchyService.loadChildren}, which is designed to receive a fresh
 * {@link ReportCountSource} per call. The {@code diagnosisCode} argument of
 * the interface is unused here (that single-code filter is a different
 * mechanism); the code set is captured in the constructor instead.
 */
public final class Form13ByDiseaseGeographyCountSource implements ReportCountSource<Form13Metric> {

    private final Form13ReportRepository form13ReportRepository;
    private final Set<String> icd10Codes;

    public Form13ByDiseaseGeographyCountSource(Form13ReportRepository form13ReportRepository, Set<String> icd10Codes) {
        this.form13ReportRepository = form13ReportRepository;
        this.icd10Codes = icd10Codes;
    }

    @Override
    public Form13Metric total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form13ReportRepository.countTotalForCodes(
                organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
        );
    }

    @Override
    public Map<Long, Form13Metric> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form13ReportRepository
                .countGroupedByOrganizationForCodes(
                        organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
                )
                .stream()
                .collect(Collectors.toMap(
                        Form13OrganizationCountProjection::organizationId,
                        p -> new Form13Metric(p.total(), p.under14(), p.under18())
                ));
    }

    @Override
    public Form13Metric empty() {
        return Form13Metric.EMPTY;
    }

    @Override
    public Form13Metric merge(Form13Metric a, Form13Metric b) {
        return a.plus(b);
    }
}
