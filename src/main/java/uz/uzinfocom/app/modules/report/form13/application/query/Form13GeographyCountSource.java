package uz.uzinfocom.app.modules.report.form13.application.query;

import uz.uzinfocom.app.modules.report.shared.ReportCaseCondition;
import uz.uzinfocom.app.modules.report.shared.ReportRowRule;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13NodeCounts;
import uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository.Form13ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ReportCountSource} strategy for one "Form 13" request: it counts
 * confirmed cases per {@code FORM_13} catalog entry (a whole {@link
 * Form13NodeCounts} row of per-disease {@link Form13Metric}s), bucketed
 * region→district→organization by the shared {@code ReportHierarchyService}.
 * <p>
 * Deliberately <b>not</b> a Spring bean — it is constructed per request in
 * {@code Form13ReportQueryService} with the ICD-10 code set of every {@code
 * FORM_13} catalog entry, then handed to the hierarchy engine, which is
 * designed to receive a fresh {@link ReportCountSource} per call. The {@code
 * diagnosisCode} argument of the interface is unused here — the disease
 * grouping is the code sets captured in the constructor.
 */
public final class Form13GeographyCountSource implements ReportCountSource<Form13NodeCounts> {

    private final Form13ReportRepository form13ReportRepository;

    /** Row condition → (manual-report entry id → its normalized ICD-10 code set). */
    private final Map<ReportCaseCondition, Map<Long, Set<String>>> codesByCondition;

    public Form13GeographyCountSource(
            Form13ReportRepository form13ReportRepository, Map<Long, ReportRowRule> rules
    ) {
        this.form13ReportRepository = form13ReportRepository;
        this.codesByCondition = ReportRowRule.codesByCondition(rules);
    }

    @Override
    public Form13NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<Long, Form13Metric> byEntryId = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<String, Form13Metric> byCode = new HashMap<>();
            form13ReportRepository
                    .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive(), condition)
                    .forEach(p -> byCode.merge(
                            p.code().toUpperCase(Locale.ROOT),
                            new Form13Metric(p.total(), p.under14(), p.under18()),
                            Form13Metric::plus
                    ));
            byEntryId.putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form13Metric.EMPTY, Form13Metric::plus));
        });
        return new Form13NodeCounts(byEntryId);
    }

    @Override
    public Map<Long, Form13NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<Long, Form13Metric>> byOrgThenEntry = new HashMap<>();
        codesByCondition.forEach((condition, codesByEntryId) -> {
            Map<Long, Map<String, Form13Metric>> byOrgThenCode = new HashMap<>();
            form13ReportRepository
                    .countGroupedByOrganizationAndDiagnosisCode(
                            organizationIds, range.fromInclusive(), range.toExclusive(), condition
                    )
                    .forEach(p -> byOrgThenCode
                            .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                            .merge(p.code().toUpperCase(Locale.ROOT), new Form13Metric(p.total(), p.under14(), p.under18()), Form13Metric::plus));

            byOrgThenCode.forEach((organizationId, byCode) -> byOrgThenEntry
                    .computeIfAbsent(organizationId, _ -> new HashMap<>())
                    .putAll(ReportRowRule.sumByEntry(codesByEntryId, byCode, Form13Metric.EMPTY, Form13Metric::plus)));
        });

        Map<Long, Form13NodeCounts> result = new HashMap<>();
        byOrgThenEntry.forEach((organizationId, byEntryId) -> result.put(organizationId, new Form13NodeCounts(byEntryId)));
        return result;
    }

    @Override
    public Form13NodeCounts empty() {
        return Form13NodeCounts.EMPTY;
    }

    @Override
    public Form13NodeCounts merge(Form13NodeCounts a, Form13NodeCounts b) {
        return a.plus(b);
    }
}
