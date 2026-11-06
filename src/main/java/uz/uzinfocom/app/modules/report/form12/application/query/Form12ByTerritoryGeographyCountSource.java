package uz.uzinfocom.app.modules.report.form12.application.query;

import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12NodeCounts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12OrganizationDiagnosisCountProjection;
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

    /** Manual-report entry id → its normalized (trimmed, upper-case) ICD-10 code set. */
    private final Map<Long, Set<String>> codesByEntryId;

    public Form12ByTerritoryGeographyCountSource(
            Form12ReportRepository form12ReportRepository, Map<Long, Set<String>> codesByEntryId
    ) {
        this.form12ReportRepository = form12ReportRepository;
        this.codesByEntryId = codesByEntryId;
    }

    @Override
    public Form12NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<String, Form12Counts> byCode = new HashMap<>();
        for (Form12DiagnosisCountProjection p : form12ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())) {
            byCode.merge(
                    p.code().toUpperCase(Locale.ROOT),
                    new Form12Counts(p.total(), p.under14(), p.under18()),
                    Form12Counts::plus
            );
        }
        return rollUp(byCode);
    }

    @Override
    public Map<Long, Form12NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<String, Form12Counts>> byOrgThenCode = new HashMap<>();
        for (Form12OrganizationDiagnosisCountProjection p : form12ReportRepository
                .countGroupedByOrganizationAndDiagnosisCode(
                        organizationIds, range.fromInclusive(), range.toExclusive()
                )) {
            byOrgThenCode
                    .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                    .merge(p.code().toUpperCase(Locale.ROOT), p.counts(), Form12Counts::plus);
        }

        Map<Long, Form12NodeCounts> result = new HashMap<>();
        byOrgThenCode.forEach((organizationId, byCode) -> result.put(organizationId, rollUp(byCode)));
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

    /**
     * Fold a per-ICD-10-code count map into a per-entry map by summing, for
     * every {@code FORM_12} catalog entry, the counts of the codes in its set.
     * A case whose code sits in two entries' sets counts once in each column —
     * same independent-per-entry roll-up as {@code Form12ReportQueryService}.
     */
    private Form12NodeCounts rollUp(Map<String, Form12Counts> byCode) {
        Map<Long, Form12Counts> byEntryId = new HashMap<>();
        codesByEntryId.forEach((entryId, codes) -> {
            Form12Counts sum = Form12Counts.EMPTY;
            for (String code : codes) {
                Form12Counts counts = byCode.get(code);
                if (counts != null) {
                    sum = sum.plus(counts);
                }
            }
            byEntryId.put(entryId, sum);
        });
        return new Form12NodeCounts(byEntryId);
    }
}
