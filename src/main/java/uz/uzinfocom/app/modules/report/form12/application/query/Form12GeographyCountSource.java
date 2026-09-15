package uz.uzinfocom.app.modules.report.form12.application.query;

import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form12.infrastructure.persistence.repository.Form12ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link ReportCountSource} strategy for a single "Form 12" geography
 * drill-down: it counts confirmed cases whose diagnosis is in one nosological
 * form's ICD-10 code set, bucketed region→district→organization by the shared
 * {@code ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — it is constructed per request in
 * {@code Form12ReportQueryService} with the code set of the catalog entry
 * being expanded, then handed to {@code ReportHierarchyService.loadChildren},
 * which is designed to receive a fresh {@link ReportCountSource} per call. The
 * {@code diagnosisCode} argument of the interface is unused here (that
 * single-code filter is a different mechanism); the code set is captured in
 * the constructor instead.
 */
public final class Form12GeographyCountSource implements ReportCountSource<Form12Counts> {

    private final Form12ReportRepository form12ReportRepository;
    private final Set<String> icd10Codes;

    public Form12GeographyCountSource(Form12ReportRepository form12ReportRepository, Set<String> icd10Codes) {
        this.form12ReportRepository = form12ReportRepository;
        this.icd10Codes = icd10Codes;
    }

    @Override
    public Form12Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form12ReportRepository.countTotalForCodes(
                organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
        );
    }

    @Override
    public Map<Long, Form12Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form12ReportRepository
                .countGroupedByOrganizationForCodes(
                        organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
                )
                .stream()
                .collect(Collectors.toMap(
                        Form12OrganizationCountProjection::organizationId,
                        p -> new Form12Counts(p.total(), p.under14(), p.under18())
                ));
    }

    @Override
    public Form12Counts empty() {
        return Form12Counts.EMPTY;
    }

    @Override
    public Form12Counts merge(Form12Counts a, Form12Counts b) {
        return a.plus(b);
    }
}
