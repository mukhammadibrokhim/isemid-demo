package uz.uzinfocom.app.modules.report.form282.application.query;

import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282Counts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282NodeCounts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282OrganizationDiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form282.infrastructure.persistence.repository.Form282ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
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

    /** Manual-report entry id → its normalized (trimmed, upper-case) ICD-10 code set. */
    private final Map<Long, Set<String>> codesByEntryId;

    public Form282ByTerritoryGeographyCountSource(
            Form282ReportRepository form282ReportRepository, Map<Long, Set<String>> codesByEntryId
    ) {
        this.form282ReportRepository = form282ReportRepository;
        this.codesByEntryId = codesByEntryId;
    }

    @Override
    public Form282NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<String, Form282Counts> byCode = new HashMap<>();
        for (Form282DiagnosisCountProjection p : form282ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())) {
            byCode.merge(p.code(), p.counts(), Form282Counts::plus);
        }
        return rollUp(byCode);
    }

    @Override
    public Map<Long, Form282NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<String, Form282Counts>> byOrgThenCode = new HashMap<>();
        for (Form282OrganizationDiagnosisCountProjection p : form282ReportRepository
                .countGroupedByOrganizationAndDiagnosisCode(
                        organizationIds, range.fromInclusive(), range.toExclusive()
                )) {
            byOrgThenCode
                    .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                    .merge(p.code(), p.counts(), Form282Counts::plus);
        }

        Map<Long, Form282NodeCounts> result = new HashMap<>();
        byOrgThenCode.forEach((organizationId, byCode) -> result.put(organizationId, rollUp(byCode)));
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

    /**
     * Fold a per-ICD-10-code count map into a per-entry map by summing, for
     * every {@code FORM_28_2} catalog entry, the counts of the codes in its
     * set. A case whose code sits in two entries' sets counts once in each
     * column — same independent-per-entry roll-up as {@code
     * Form282ReportQueryService}.
     */
    private Form282NodeCounts rollUp(Map<String, Form282Counts> byCode) {
        Map<Long, Form282Counts> byEntryId = new HashMap<>();
        codesByEntryId.forEach((entryId, codes) -> {
            Form282Counts sum = Form282Counts.EMPTY;
            for (String code : codes) {
                Form282Counts counts = byCode.get(code);
                if (counts != null) {
                    sum = sum.plus(counts);
                }
            }
            byEntryId.put(entryId, sum);
        });
        return new Form282NodeCounts(byEntryId);
    }
}
