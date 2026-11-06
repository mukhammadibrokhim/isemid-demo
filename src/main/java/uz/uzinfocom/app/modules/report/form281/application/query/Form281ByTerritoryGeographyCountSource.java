package uz.uzinfocom.app.modules.report.form281.application.query;

import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281Counts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281NodeCounts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281OrganizationDiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form281.infrastructure.persistence.repository.Form281ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;

import java.util.HashMap;
import java.util.List;
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

    /** Manual-report entry id → its normalized (trimmed, upper-case) ICD-10 code set. */
    private final Map<Long, Set<String>> codesByEntryId;

    public Form281ByTerritoryGeographyCountSource(
            Form281ReportRepository form281ReportRepository, Map<Long, Set<String>> codesByEntryId
    ) {
        this.form281ReportRepository = form281ReportRepository;
        this.codesByEntryId = codesByEntryId;
    }

    @Override
    public Form281NodeCounts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        Map<String, Form281Counts> byCode = new HashMap<>();
        for (Form281DiagnosisCountProjection p : form281ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())) {
            byCode.merge(p.code(), p.counts(), Form281Counts::plus);
        }
        return rollUp(byCode);
    }

    @Override
    public Map<Long, Form281NodeCounts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        Map<Long, Map<String, Form281Counts>> byOrgThenCode = new HashMap<>();
        for (Form281OrganizationDiagnosisCountProjection p : form281ReportRepository
                .countGroupedByOrganizationAndDiagnosisCode(
                        organizationIds, range.fromInclusive(), range.toExclusive()
                )) {
            byOrgThenCode
                    .computeIfAbsent(p.organizationId(), _ -> new HashMap<>())
                    .merge(p.code(), p.counts(), Form281Counts::plus);
        }

        Map<Long, Form281NodeCounts> result = new HashMap<>();
        byOrgThenCode.forEach((organizationId, byCode) -> result.put(organizationId, rollUp(byCode)));
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

    /**
     * Fold a per-ICD-10-code count map into a per-entry map by summing, for
     * every {@code FORM_28_1} catalog entry, the counts of the codes in its
     * set. A case whose code sits in two entries' sets counts once in each
     * column — same independent-per-entry roll-up as {@code
     * Form281ReportQueryService}.
     */
    private Form281NodeCounts rollUp(Map<String, Form281Counts> byCode) {
        Map<Long, Form281Counts> byEntryId = new HashMap<>();
        codesByEntryId.forEach((entryId, codes) -> {
            Form281Counts sum = Form281Counts.EMPTY;
            for (String code : codes) {
                Form281Counts counts = byCode.get(code);
                if (counts != null) {
                    sum = sum.plus(counts);
                }
            }
            byEntryId.put(entryId, sum);
        });
        return new Form281NodeCounts(byEntryId);
    }
}
