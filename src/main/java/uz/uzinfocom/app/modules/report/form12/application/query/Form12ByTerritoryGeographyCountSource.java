package uz.uzinfocom.app.modules.report.form12.application.query;

import uz.uzinfocom.app.modules.report.shared.ReportCaseCondition;
import uz.uzinfocom.app.modules.report.shared.ReportRowRule;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12NodeCounts;
import uz.uzinfocom.app.modules.report.form12.infrastructure.persistence.repository.Form12ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for the "Form 12 by territory" view —
 * the same relationship "Form 28.1 by territory" has to "Form 28.1": it counts
 * confirmed cases per {@code FORM_12} catalog entry (a whole {@link
 * Form12NodeCounts} row of per-nosological-form {@link Form12Counts}),
 * bucketed region→district→organization by the shared {@code
 * ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — constructed per request in {@code
 * Form12ByTerritoryReportQueryService} with the ICD-10 code set of every
 * {@code FORM_12} catalog entry, then handed to the hierarchy engine, which is
 * designed to receive a fresh {@link ReportCountSource} per call. The {@code
 * diagnosisCode} argument of the interface is unused here — the disease
 * grouping is the code sets captured in the constructor.
 */
public final class Form12ByTerritoryGeographyCountSource implements ReportCountSource<Form12NodeCounts> {

    private final Form12ReportRepository form12ReportRepository;

    /** Row condition → (manual-report entry id → its normalized ICD-10 code set). */
    private final Map<ReportCaseCondition, Map<Long, Set<String>>> codesByCondition;

    public Form12ByTerritoryGeographyCountSource(
            Form12ReportRepository form12ReportRepository, Map<Long, ReportRowRule> rules
    ) {
        this.form12ReportRepository = form12ReportRepository;
        this.codesByCondition = ReportRowRule.codesByCondition(rules);
    }

    @Override
    public Form12NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<Long, Form12Counts> byEntryId = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<String, Form12Counts> byCode = new HashMap<>();
            form12ReportRepository
                    .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive(), condition)
                    .forEach(p -> byCode.merge(
                            p.code().toUpperCase(Locale.ROOT),
                            new Form12Counts(p.total(), p.under14(), p.under18()),
                            Form12Counts::plus
                    ));
            byEntryId.putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form12Counts.EMPTY, Form12Counts::plus));
        });
        return new Form12NodeCounts(byEntryId);
    }

    @Override
    public Map<Long, Form12NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<Long, Form12Counts>> byOrgThenEntry = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<Long, Map<String, Form12Counts>> byOrgThenCode = new HashMap<>();
            form12ReportRepository
                    .countGroupedByOrganizationAndDiagnosisCode(
                            organizationIds, range.fromInclusive(), range.toExclusive(), condition
                    )
                    .forEach(p -> byOrgThenCode
                            .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                            .merge(p.code().toUpperCase(Locale.ROOT), p.counts(), Form12Counts::plus));

            byOrgThenCode.forEach((organizationId, byCode) -> byOrgThenEntry
                    .computeIfAbsent(organizationId, _ -> new HashMap<>())
                    .putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form12Counts.EMPTY, Form12Counts::plus)));
        });

        Map<Long, Form12NodeCounts> result = new HashMap<>();
        byOrgThenEntry.forEach((organizationId, byEntryId) -> result.put(organizationId, new Form12NodeCounts(byEntryId)));
        return result;
    }

    @Override
    public Form12NodeCounts empty() {
        return Form12NodeCounts.EMPTY;
    }

    @Override
    public Form12NodeCounts merge(Form12NodeCounts a, Form12NodeCounts b) {
        return a.plus(b);
    }
}
