package uz.uzinfocom.app.modules.report.form13.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13NodeCounts;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository.Form13ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Form 13" — the geography-first counterpart of "Form 12". Same base material
 * (confirmed, {@code status = 'APPROVED'}, {@code form058} + {@code form058_1};
 * disease set = the manual-report catalog), but transposed: rows are geography
 * (republic→region→district→organization, one level per call via the shared
 * {@link ReportHierarchyService}) and columns are diseases — every {@link
 * ManualReportResponse} tagged {@value #MANUAL_REPORT_TYPE}. A case's diagnosis
 * is its <b>confirmed final code alone</b> ({@code form058(_1).final_icd10_code},
 * no fallback to the initial {@code icd10_code}) — a case with no final code
 * recorded is not counted. Each disease cell shows the chosen period ("Joriy
 * yil") next to the same calendar dates one year earlier ("O'tgan yil") in
 * three metrics — total / under 14 / under 18. No delta column.
 * <p>
 * Every response row carries its {@code diseases[]} cells in the same order —
 * the {@code FORM_13} entries sorted by {@code code} — so the frontend renders
 * one stable set of columns across the whole table (and the trailing "Jami"
 * row).
 */
@Service
@RequiredArgsConstructor
public class Form13ReportQueryService {

    /** The {@code ManualReport.reportTypes} tag a catalog entry must carry to appear as a "Form 13" column. */
    public static final String MANUAL_REPORT_TYPE = "FORM_13";

    private final Form13ReportRepository form13ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final LocalizedTextResolver localizedTextResolver;

    public List<Form13ReportNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form13GeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form13NodeCounts>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form13NodeCounts>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(from, to, 1), null
        );

        return zip(currentNodes, previousNodes, entries);
    }

    public List<Form13ReportNodeResponse> getChildren(
            String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form13GeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form13NodeCounts>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form13NodeCounts>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to, 1), null
        );

        return zip(currentNodes, previousNodes, entries);
    }

    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private Form13GeographyCountSource countSource(List<ManualReportResponse> entries) {
        Map<Long, Set<String>> codesByEntryId = entries.stream()
                .collect(Collectors.toMap(ManualReportResponse::id, e -> normalizeCodes(e.icd10Codes())));
        return new Form13GeographyCountSource(form13ReportRepository, codesByEntryId);
    }

    private List<Form13ReportNodeResponse> zip(
            List<ReportHierarchyNode<Form13NodeCounts>> currentNodes,
            List<ReportHierarchyNode<Form13NodeCounts>> previousNodes,
            List<ManualReportResponse> entries
    ) {
        Map<String, Form13NodeCounts> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts, (a, _) -> a));

        return currentNodes.stream()
                .map(node -> new Form13ReportNodeResponse(
                        node.code(),
                        node.name(),
                        node.hasChildren(),
                        diseaseCells(
                                entries, node.counts(),
                                previousByCode.getOrDefault(node.code(), Form13NodeCounts.EMPTY)
                        )
                ))
                .toList();
    }

    private List<Form13DiseaseCellResponse> diseaseCells(
            List<ManualReportResponse> entries, Form13NodeCounts current, Form13NodeCounts previous
    ) {
        return entries.stream()
                .map(entry -> {
                    Form13Metric cur = current.metric(entry.id());
                    Form13Metric prev = previous.metric(entry.id());
                    return new Form13DiseaseCellResponse(
                            entry.id(), entry.code(), localizedName(entry), entry.shortName(),
                            prev.total(), cur.total(),
                            prev.under14(), cur.under14(),
                            prev.under18(), cur.under18()
                    );
                })
                .toList();
    }

    private Set<String> normalizeCodes(Set<String> codes) {
        if (codes == null) {
            return Set.of();
        }
        return codes.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private String localizedName(ManualReportResponse entry) {
        return localizedTextResolver.resolve(
                entry.nameUz(), entry.nameUzCyril(), entry.nameRu(), entry.nameKaa()
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
