package uz.uzinfocom.app.modules.report.form281.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281Counts;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281DiagnosisCountProjection;
import uz.uzinfocom.app.modules.report.form281.application.query.dto.Form281ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form281.infrastructure.persistence.repository.Form281ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ResolvedReportNode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Form 28.1" — «Ayrim yuqumli va parazitar kasalliklar haqida ma'lumotlar».
 * Structurally a clone of {@code Form12ReportQueryService}: the root level is a
 * flat list of <b>nosological forms</b> — every {@link ManualReportResponse} in
 * the admin-managed manual-report catalog tagged {@value #MANUAL_REPORT_TYPE} —
 * and expanding a row drills into geography
 * (republic→region→district→organization), one level per call, delegated to the
 * shared {@link ReportHierarchyService} with a per-request {@link
 * Form281GeographyCountSource}. Each row's numbers are confirmed ({@code status
 * = 'APPROVED'}, {@code deleted = false}) {@code form058} / {@code form058_1}
 * cases whose confirmed final diagnosis ({@code final_icd10_code}, no fallback
 * to the initial {@code icd10_code}) is in that entry's ICD-10 code set.
 * <p>
 * Unlike Form 12 there is <b>no</b> year-over-year comparison — one arbitrary
 * period {@code [from, to]} only — and the metrics are the reference form's
 * varaqa columns (see {@link Form281Counts}): total / female / under-18 /
 * under-15 / under-1 / 1–2 / 3–5, and the same age cuts again for the rural
 * population. Last row is «Jami», the sum of only the rows whose catalog entry
 * has {@code includeInTotal} set.
 */
@Service
@RequiredArgsConstructor
public class Form281ReportQueryService {

    /** The {@code ManualReport.reportTypes} tag a catalog entry must carry to appear in Form 28.1. */
    public static final String MANUAL_REPORT_TYPE = "FORM_28_1";

    private static final String TOTAL_CODE = "TOTAL";

    private final Form281ReportRepository form281ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final LocalizedTextResolver localizedTextResolver;
    private final MessageResolver messageResolver;

    public List<Form281ReportNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();

        ResolvedReportNode scopeNode = reportHierarchyService.resolveNode(currentOrganization, null, null);
        List<Long> organizationIds = scopeNode.organizationIds();
        boolean hasChildren =
                organizationScopeResolver.resolve(currentOrganization).mode() != OrganizationScopeMode.ORGANIZATION;

        ReportDateRange range = reportDateRangeResolver.resolve(from, to);
        Map<String, Form281Counts> countsByCode = form281ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())
                .stream()
                .collect(Collectors.toMap(
                        p -> p.code().toUpperCase(Locale.ROOT),
                        Form281DiagnosisCountProjection::counts,
                        Form281Counts::plus
                ));

        List<ManualReportResponse> entries = orderedEntries();

        List<Form281ReportNodeResponse> rows = entries.stream()
                .map(entry -> row(
                        String.valueOf(entry.id()), localizedName(entry), entry.code(), entry.shortName(),
                        hasChildren, rollUp(entry, countsByCode)
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        rows.add(totalRow(entries, countsByCode));
        return rows;
    }

    public List<Form281ReportNodeResponse> getChildren(
            Long manualReportId, String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        ManualReportResponse entry = manualReportQueryService.getById(manualReportId);
        Set<String> codes = normalizeCodes(entry.icd10Codes());
        Form281GeographyCountSource countSource = new Form281GeographyCountSource(form281ReportRepository, codes);

        List<ReportHierarchyNode<Form281Counts>> nodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );

        return nodes.stream()
                .map(node -> row(
                        node.code(), node.name(), null, null, node.hasChildren(), node.counts()
                ))
                .toList();
    }

    private List<ManualReportResponse> orderedEntries() {
        return manualReportQueryService.findByReportType(MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();
    }

    private Form281ReportNodeResponse totalRow(
            List<ManualReportResponse> entries, Map<String, Form281Counts> countsByCode
    ) {
        Form281Counts total = entries.stream()
                .filter(e -> Boolean.TRUE.equals(e.includeInTotal()))
                .map(e -> rollUp(e, countsByCode))
                .reduce(Form281Counts.EMPTY, Form281Counts::plus);

        return row(TOTAL_CODE, messageResolver.resolve("report.scope.total"), null, null, false, total);
    }

    /** Sum the per-code counts for every ICD-10 code in one catalog entry's set. */
    private Form281Counts rollUp(ManualReportResponse entry, Map<String, Form281Counts> countsByCode) {
        return normalizeCodes(entry.icd10Codes()).stream()
                .map(code -> countsByCode.getOrDefault(code, Form281Counts.EMPTY))
                .reduce(Form281Counts.EMPTY, Form281Counts::plus);
    }

    private Form281ReportNodeResponse row(
            String code, String name, String rowCode, String icd10Display, boolean hasChildren, Form281Counts c
    ) {
        return new Form281ReportNodeResponse(
                code, name, rowCode, icd10Display, hasChildren,
                c.total(), c.female(), c.under18(), c.under15(), c.under1(), c.age1to2(), c.age3to5(),
                c.ruralTotal(), c.ruralUnder18(), c.ruralUnder15(), c.ruralUnder1(), c.ruralAge1to2(), c.ruralAge3to5()
        );
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
