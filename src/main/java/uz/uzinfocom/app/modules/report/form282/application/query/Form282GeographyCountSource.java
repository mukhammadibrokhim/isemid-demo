package uz.uzinfocom.app.modules.report.form282.application.query;

import uz.uzinfocom.app.modules.report.shared.ReportCaseCondition;
import uz.uzinfocom.app.modules.report.shared.ReportRowRule;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282Counts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form282.infrastructure.persistence.repository.Form282ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link ReportCountSource} strategy for a single "Form 28.2" geography
 * drill-down: it counts confirmed cases whose confirmed final diagnosis is in
 * one nosological form's ICD-10 code set, bucketed region→district→organization
 * by the shared {@code ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * Form282ReportQueryService} with the code set of the catalog entry being
 * expanded. The {@code diagnosisCode} argument of the interface is unused here —
 * the code set is captured in the constructor instead.
 */
public final class Form282GeographyCountSource implements ReportCountSource<Form282Counts> {

    private final Form282ReportRepository form282ReportRepository;
    private final Set<String> icd10Codes;
    private final ReportCaseCondition condition;

    public Form282GeographyCountSource(Form282ReportRepository form282ReportRepository, ReportRowRule rule) {
        this.form282ReportRepository = form282ReportRepository;
        this.icd10Codes = rule.icd10Codes();
        this.condition = rule.condition();
    }

    @Override
    public Form282Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form282ReportRepository.countTotalForCodes(
                organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes, condition
        );
    }

    @Override
    public Map<Long, Form282Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form282ReportRepository
                .countGroupedByOrganizationForCodes(
                        organizationIds, range.fromInclusive(), range.toExclusive(), icd10Codes, condition
                )
                .stream()
                .collect(Collectors.toMap(
                        Form282OrganizationCountProjection::organizationId,
                        Form282OrganizationCountProjection::counts,
                        Form282Counts::plus
                ));
    }

    @Override
    public Form282Counts empty() {
        return Form282Counts.EMPTY;
    }

    @Override
    public Form282Counts merge(Form282Counts a, Form282Counts b) {
        return a.plus(b);
    }
}
