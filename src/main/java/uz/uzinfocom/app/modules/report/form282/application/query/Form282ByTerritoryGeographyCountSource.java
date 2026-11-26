package uz.uzinfocom.app.modules.report.form282.application.query;

import uz.uzinfocom.app.modules.report.shared.ReportCaseCondition;
import uz.uzinfocom.app.modules.report.shared.ReportRowRule;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282Counts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282NodeCounts;
import uz.uzinfocom.app.modules.report.form282.infrastructure.persistence.repository.Form282ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for the "Form 28.2 by territory" view
 * — the geography-first counterpart of "Form 28.2", the same relationship
 * "Form 13" has to "Form 12": it counts confirmed cases per {@code FORM_28_2}
 * catalog entry (a whole {@link Form282NodeCounts} row of per-disease {@link
 * Form282Counts}), bucketed region→district→organization by the shared {@code
 * ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * Form282ByTerritoryReportQueryService} with the ICD-10 code set of every
 * {@code FORM_28_2} catalog entry, then handed to the hierarchy engine, which
 * is designed to receive a fresh {@link ReportCountSource} per call. The
 * {@code diagnosisCode} argument of the interface is unused here — the
 * disease grouping is the code sets captured in the constructor.
 */
public final class Form282ByTerritoryGeographyCountSource implements ReportCountSource<Form282NodeCounts> {

    private final Form282ReportRepository form282ReportRepository;

    /** Row condition → (manual-report entry id → its normalized ICD-10 code set). */
    private final Map<ReportCaseCondition, Map<Long, Set<String>>> codesByCondition;

    public Form282ByTerritoryGeographyCountSource(
            Form282ReportRepository form282ReportRepository, Map<Long, ReportRowRule> rules
    ) {
        this.form282ReportRepository = form282ReportRepository;
        this.codesByCondition = ReportRowRule.codesByCondition(rules);
    }

    @Override
    public Form282NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<Long, Form282Counts> byEntryId = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<String, Form282Counts> byCode = new HashMap<>();
            form282ReportRepository
                    .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive(), condition)
                    .forEach(p -> byCode.merge(
                            p.code().toUpperCase(Locale.ROOT), p.counts(), Form282Counts::plus
                    ));
            byEntryId.putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form282Counts.EMPTY, Form282Counts::plus));
        });
        return new Form282NodeCounts(byEntryId);
    }

    @Override
    public Map<Long, Form282NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<Long, Form282Counts>> byOrgThenEntry = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<Long, Map<String, Form282Counts>> byOrgThenCode = new HashMap<>();
            form282ReportRepository
                    .countGroupedByOrganizationAndDiagnosisCode(
                            organizationIds, range.fromInclusive(), range.toExclusive(), condition
                    )
                    .forEach(p -> byOrgThenCode
                            .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                            .merge(p.code().toUpperCase(Locale.ROOT), p.counts(), Form282Counts::plus));

            byOrgThenCode.forEach((organizationId, byCode) -> byOrgThenEntry
                    .computeIfAbsent(organizationId, _ -> new HashMap<>())
                    .putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form282Counts.EMPTY, Form282Counts::plus)));
        });

        Map<Long, Form282NodeCounts> result = new HashMap<>();
        byOrgThenEntry.forEach((organizationId, byEntryId) -> result.put(organizationId, new Form282NodeCounts(byEntryId)));
        return result;
    }

    @Override
    public Form282NodeCounts empty() {
        return Form282NodeCounts.EMPTY;
    }

    @Override
    public Form282NodeCounts merge(Form282NodeCounts a, Form282NodeCounts b) {
        return a.plus(b);
    }
}
