package uz.uzinfocom.app.modules.report.form282.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282ByTerritoryNodeResponse;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282Counts;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282DiseaseCellResponse;
import uz.uzinfocom.app.modules.report.form282.application.query.dto.Form282NodeCounts;
import uz.uzinfocom.app.modules.report.form282.infrastructure.persistence.repository.Form282ReportRepository;
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
 * "Form 28.2 by territory" — the geography-first counterpart of "Form 28.2",
 * the same relationship "Form 13" has to "Form 12". Same base material
 * (confirmed, {@code status = 'APPROVED'}, {@code deleted = false}, {@code
 * form058} + {@code form058_1}; disease set = the {@code FORM_28_2}-tagged
 * manual-report catalog entries), but transposed: rows are geography
 * (republic→region→district→organization, one level per call via the shared
 * {@link ReportHierarchyService}) and columns are diseases. A case's diagnosis
 * is its <b>confirmed final code alone</b> ({@code
 * form058(_1).final_icd10_code}, no fallback to the initial {@code
 * icd10_code}) — a case with no final code recorded is not counted. One
 * arbitrary period {@code [from, to]} only — no year-over-year, matching
 * disease-first "Form 28.2" — and each disease cell carries the reference
 * form's own varaqa columns (see {@link Form282Counts}).
 * <p>
 * Every response row carries its {@code diseases[]} cells in the same order —
 * the {@code FORM_28_2} entries sorted by {@code code} — so the frontend
 * renders one stable set of columns across the whole table (and the trailing
 * "Jami" row, folded in by {@link ReportHierarchyService#loadRootBreakdown}).
 */
@Service
@RequiredArgsConstructor
public class Form282ByTerritoryReportQueryService {

    private final Form282ReportRepository form282ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final LocalizedTextResolver localizedTextResolver;

    public List<Form282ByTerritoryNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form282ByTerritoryGeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form282NodeCounts>> nodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, countSource, reportDateRangeResolver.resolve(from, to), null
        );

        return nodes.stream().map(node -> toResponse(node, entries)).toList();
    }

    public List<Form282ByTerritoryNodeResponse> getChildren(
            String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        List<ManualReportResponse> entries = orderedEntries();
        Form282ByTerritoryGeographyCountSource countSource = countSource(entries);

        List<ReportHierarchyNode<Form282NodeCounts>> nodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );

        return nodes.stream().map(node -> toResponse(node, entries)).toList();
    }

    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(Form282ReportQueryService.MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private Form282ByTerritoryGeographyCountSource countSource(List<ManualReportResponse> entries) {
        Map<Long, Set<String>> codesByEntryId = entries.stream()
                .collect(Collectors.toMap(ManualReportResponse::id, e -> normalizeCodes(e.icd10Codes())));
        return new Form282ByTerritoryGeographyCountSource(form282ReportRepository, codesByEntryId);
    }

    private Form282ByTerritoryNodeResponse toResponse(
            ReportHierarchyNode<Form282NodeCounts> node, List<ManualReportResponse> entries
    ) {
        return new Form282ByTerritoryNodeResponse(
                node.code(), node.name(), node.hasChildren(), diseaseCells(entries, node.counts())
        );
    }

    private List<Form282DiseaseCellResponse> diseaseCells(List<ManualReportResponse> entries, Form282NodeCounts counts) {
        return entries.stream()
                .map(entry -> {
                    Form282Counts c = counts.metric(entry.id());
                    return new Form282DiseaseCellResponse(
                            entry.id(), entry.code(), localizedName(entry), entry.shortName(),
                            c.total(), c.under18(), c.underOneMonth(), c.oneMonthToUnderOneYear()
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
