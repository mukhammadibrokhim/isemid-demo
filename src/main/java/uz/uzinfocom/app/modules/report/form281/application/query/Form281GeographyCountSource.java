package uz.uzinfocom.app.modules.report.form281.application.query;

import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281Counts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form281.infrastructure.persistence.repository.Form281ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link ReportCountSource} strategy for a single "Form 28.1" geography
 * drill-down: it counts confirmed cases whose confirmed final diagnosis is in
 * one nosological form's ICD-10 code set, bucketed region→district→organization
 * by the shared {@code ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * Form281ReportQueryService} with the code set of the catalog entry being
 * expanded, then handed to {@code ReportHierarchyService.loadChildren}, which is
 * designed to receive a fresh {@link ReportCountSource} per call. The {@code
 * diagnosisCode} argument of the interface is unused here — the code set is
 * captured in the constructor instead.
 */
public final class Form281GeographyCountSource implements ReportCountSource<Form281Counts> {

    private final Form281ReportRepository form281ReportRepository;
    private final Set<String> icd10Codes;

    public Form281GeographyCountSource(Form281ReportRepository form281ReportRepository, Set<String> icd10Codes) {
        this.form281ReportRepository = form281ReportRepository;
        this.icd10Codes = icd10Codes;
    }

    @Override
    public Form281Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form281ReportRepository.countTotalForCodes(
                organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
        );
    }

    @Override
    public Map<Long, Form281Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form281ReportRepository
                .countGroupedByOrganizationForCodes(
                        organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes
                )
                .stream()
                .collect(Collectors.toMap(
                        Form281OrganizationCountProjection::organizationId,
                        Form281OrganizationCountProjection::counts,
                        Form281Counts::plus
                ));
    }

    @Override
    public Form281Counts empty() {
        return Form281Counts.EMPTY;
    }

    @Override
    public Form281Counts merge(Form281Counts a, Form281Counts b) {
        return a.plus(b);
    }
}
