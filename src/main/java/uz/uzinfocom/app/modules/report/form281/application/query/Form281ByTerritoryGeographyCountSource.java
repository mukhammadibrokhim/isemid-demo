package uz.uzinfocom.app.modules.report.form281.application.query;

import uz.uzinfocom.app.modules.report.shared.ReportCaseCondition;
import uz.uzinfocom.app.modules.report.shared.ReportRowRule;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281Counts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281NodeCounts;
import uz.uzinfocom.app.modules.report.form281.infrastructure.persistence.repository.Form281ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for the "Form 28.1 by territory" view
 * — the geography-first counterpart of "Form 28.1", the same relationship
 * "Form 13" has to "Form 12": it counts confirmed cases per {@code FORM_28_1}
 * catalog entry (a whole {@link Form281NodeCounts} row of per-disease {@link
 * Form281Counts}), bucketed region→district→organization by the shared {@code
 * ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * Form281ByTerritoryReportQueryService} with the ICD-10 code set of every
 * {@code FORM_28_1} catalog entry, then handed to the hierarchy engine, which
 * is designed to receive a fresh {@link ReportCountSource} per call. The
 * {@code diagnosisCode} argument of the interface is unused here — the
 * disease grouping is the code sets captured in the constructor.
 */
public final class Form281ByTerritoryGeographyCountSource implements ReportCountSource<Form281NodeCounts> {

    private final Form281ReportRepository form281ReportRepository;

    /** Row condition → (manual-report entry id → its normalized ICD-10 code set). */
    private final Map<ReportCaseCondition, Map<Long, Set<String>>> codesByCondition;

    public Form281ByTerritoryGeographyCountSource(
            Form281ReportRepository form281ReportRepository, Map<Long, ReportRowRule> rules
    ) {
        this.form281ReportRepository = form281ReportRepository;
        this.codesByCondition = ReportRowRule.codesByCondition(rules);
    }

    @Override
    public Form281NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<Long, Form281Counts> byEntryId = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<String, Form281Counts> byCode = new HashMap<>();
            form281ReportRepository
                    .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive(), condition)
                    .forEach(p -> byCode.merge(
                            p.code().toUpperCase(Locale.ROOT), p.counts(), Form281Counts::plus
                    ));
            byEntryId.putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form281Counts.EMPTY, Form281Counts::plus));
        });
        return new Form281NodeCounts(byEntryId);
    }

    @Override
    public Map<Long, Form281NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<Long, Form281Counts>> byOrgThenEntry = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<Long, Map<String, Form281Counts>> byOrgThenCode = new HashMap<>();
            form281ReportRepository
                    .countGroupedByOrganizationAndDiagnosisCode(
                            organizationIds, range.fromInclusive(), range.toExclusive(), condition
                    )
                    .forEach(p -> byOrgThenCode
                            .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                            .merge(p.code().toUpperCase(Locale.ROOT), p.counts(), Form281Counts::plus));

            byOrgThenCode.forEach((organizationId, byCode) -> byOrgThenEntry
                    .computeIfAbsent(organizationId, _ -> new HashMap<>())
                    .putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form281Counts.EMPTY, Form281Counts::plus)));
        });

        Map<Long, Form281NodeCounts> result = new HashMap<>();
        byOrgThenEntry.forEach((organizationId, byEntryId) -> result.put(organizationId, new Form281NodeCounts(byEntryId)));
        return result;
    }

    @Override
    public Form281NodeCounts empty() {
        return Form281NodeCounts.EMPTY;
    }

    @Override
    public Form281NodeCounts merge(Form281NodeCounts a, Form281NodeCounts b) {
        return a.plus(b);
    }
}
