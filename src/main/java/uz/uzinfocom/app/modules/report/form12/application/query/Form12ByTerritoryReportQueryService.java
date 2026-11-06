package uz.uzinfocom.app.modules.report.form12.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12ByTerritoryNodeResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12NodeCounts;
import uz.uzinfocom.app.modules.report.form12.infrastructure.persistence.repository.Form12ReportRepository;
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
 * "Form 12 by territory" — the geography-first counterpart of "Form 12", the
 * same relationship "Form 28.1 by territory" has to "Form 28.1" (and "Form
 * 13" has to "Form 12" itself). Same base material (confirmed, {@code status
 * = 'APPROVED'}, {@code form058} + {@code form058_1}; disease set = the
 * {@code FORM_12}-tagged manual-report catalog entries, the same ones "Form
 * 12" itself uses), but transposed: rows are geography
 * (republic→region→district→organization, one level per call via the shared
 * {@link ReportHierarchyService}) and columns are nosological forms. A case's
 * diagnosis is its <b>confirmed final code alone</b> ({@code
 * form058(_1).final_icd10_code}, no fallback to the initial {@code
 * icd10_code}) — a case with no final code recorded is not counted. Each
 * disease cell shows the chosen period ("Joriy yil") next to the same
 * calendar dates one year earlier ("O'tgan yil") in three metrics — total /
 * under 14 / under 18 — same as "Form 13". No delta column.
 * <p>
 * Every response row carries its {@code diseases[]} cells in the same order —
 * the {@code FORM_12} entries sorted by {@code code} — so the frontend
 * renders one stable set of columns across the whole table (and the trailing
 * "Jami" row, folded in by {@link ReportHierarchyService#loadRootBreakdown}).
 */
@Service
@RequiredArgsConstructor
public class Form12ByTerritoryReportQueryService {

    private final Form12ReportRepository form12ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final LocalizedTextResolver localizedTextResolver;

    public List<Form12ByTerritoryNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form12ByTerritoryGeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form12NodeCounts>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form12NodeCounts>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(from, to, 1), null
        );

        return zip(currentNodes, previousNodes, entries);
    }

    public List<Form12ByTerritoryNodeResponse> getChildren(
            String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form12ByTerritoryGeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form12NodeCounts>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form12NodeCounts>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to, 1), null
        );

        return zip(currentNodes, previousNodes, entries);
    }

    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form12ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private Form12ByTerritoryGeographyCountSource countSource(List<ManualReportResponse> entries) {
        Map<Long, Set<String>> codesByEntryId = entries.stream()
                .collect(Collectors.toMap(ManualReportResponse::id, e -> normalizeCodes(e.icd10Codes())));
        return new Form12ByTerritoryGeographyCountSource(form12ReportRepository, codesByEntryId);
    }

    private List<Form12ByTerritoryNodeResponse> zip(
            List<ReportHierarchyNode<Form12NodeCounts>> currentNodes,
            List<ReportHierarchyNode<Form12NodeCounts>> previousNodes,
            List<ManualReportResponse> entries
    ) {
        Map<String, Form12NodeCounts> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts, (a, _) -> a));

        return currentNodes.stream()
                .map(node -> new Form12ByTerritoryNodeResponse(
                        node.code(),
                        node.name(),
                        node.hasChildren(),
                        diseaseCells(
                                entries, node.counts(),
                                previousByCode.getOrDefault(node.code(), Form12NodeCounts.EMPTY)
                        )
                ))
                .toList();
    }

    private List<Form12DiseaseCellResponse> diseaseCells(
            List<ManualReportResponse> entries, Form12NodeCounts current, Form12NodeCounts previous
    ) {
        return entries.stream()
                .map(entry -> {
                    Form12Counts cur = current.metric(entry.id());
                    Form12Counts prev = previous.metric(entry.id());
                    return new Form12DiseaseCellResponse(
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
