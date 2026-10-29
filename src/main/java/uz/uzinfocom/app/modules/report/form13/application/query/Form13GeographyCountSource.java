package uz.uzinfocom.app.modules.report.form13.application.query;

import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13NodeCounts;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13OrganizationDiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository.Form13ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
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

    /** Manual-report entry id → its normalized (trimmed, upper-case) ICD-10 code set. */
    private final Map<Long, Set<String>> codesByEntryId;

    public Form13GeographyCountSource(
            Form13ReportRepository form13ReportRepository, Map<Long, Set<String>> codesByEntryId
    ) {
        this.form13ReportRepository = form13ReportRepository;
        this.codesByEntryId = codesByEntryId;
    }

    @Override
    public Form13NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<String, Form13Metric> byCode = new HashMap<>();
        form13ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())
                .forEach(p -> byCode.merge(
                        p.code(), new Form13Metric(p.total(), p.under14(), p.under18()), Form13Metric::plus
                ));
        return rollUp(byCode);
    }

    @Override
    public Map<Long, Form13NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<String, Form13Metric>> byOrgThenCode = new HashMap<>();
        for (Form13OrganizationDiagnosisCountProjection p : form13ReportRepository
                .countGroupedByOrganizationAndDiagnosisCode(
                        organizationIds, range.fromInclusive(), range.toExclusive()
                )) {
            byOrgThenCode
                    .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                    .merge(p.code(), new Form13Metric(p.total(), p.under14(), p.under18()), Form13Metric::plus);
        }

        Map<Long, Form13NodeCounts> result = new HashMap<>();
        byOrgThenCode.forEach((organizationId, byCode) -> result.put(organizationId, rollUp(byCode)));
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

    /**
     * Fold a per-ICD-10-code count map into a per-entry map by summing, for
     * every {@code FORM_13} catalog entry, the metrics of the codes in its set.
     * A case whose code sits in two entries' sets counts once in each column —
     * same independent-per-entry roll-up as {@code Form12ReportQueryService}.
     */
    private Form13NodeCounts rollUp(Map<String, Form13Metric> byCode) {
        Map<Long, Form13Metric> byEntryId = new HashMap<>();
        codesByEntryId.forEach((entryId, codes) -> {
            Form13Metric sum = Form13Metric.EMPTY;
            for (String code : codes) {
                Form13Metric metric = byCode.get(code);
                if (metric != null) {
                    sum = sum.plus(metric);
                }
            }
            byEntryId.put(entryId, sum);
        });
        return new Form13NodeCounts(byEntryId);
    }
}
