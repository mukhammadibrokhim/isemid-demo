package uz.uzinfocom.app.modules.report.form12.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12Counts;
import uz.uzinfocom.app.modules.report.form12.application.query.dto.Form12ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form12.infrastructure.persistence.repository.Form12ReportRepository;
import uz.uzinfocom.app.modules.report.shared.*;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "Form 12" — «Nozologik shakllar bo'yicha yuqumli va parazitar kasalliklar».
 * Unlike the geography-first reports, Form 12's root level is a flat list of
 * <b>nosological forms</b>: every {@link ManualReportResponse} in the
 * admin-managed manual-report catalog tagged {@value #MANUAL_REPORT_TYPE}. Each
 * row's numbers are confirmed ({@code status = 'APPROVED'}) {@code form058} /
 * {@code form058_1} cases whose confirmed diagnosis is in that entry's ICD-10
 * code set — {@code total}, {@code under14}, {@code under18}, each shown for the
 * chosen period next to the same calendar dates one year earlier and the delta
 * ({@link ReportDateRangeResolver}'s {@code yearsAgo}), like Form 6/8/9/11.
 * <p>
 * Expanding a row drills into geography (republic→region→district→organization),
 * one level per call, for that one nosological form — delegated to the shared
 * {@link ReportHierarchyService} with a per-request {@link
 * Form12GeographyCountSource}.
 */
@Service
@RequiredArgsConstructor
public class Form12ReportQueryService {

    /**
     * The {@code ManualReport.reportTypes} tag a catalog entry must carry to appear in Form 12 (matched case-insensitively).
     */
    public static final String MANUAL_REPORT_TYPE = "FORM_12";

    private static final String TOTAL_CODE = "TOTAL";

    private final Form12ReportRepository form12ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final LocalizedTextResolver localizedTextResolver;
    private final MessageResolver messageResolver;

    public List<Form12ReportNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();

        ResolvedReportNode scopeNode = reportHierarchyService.resolveNode(currentOrganization, null, null);
        List<Long> organizationIds = scopeNode.organizationIds();
        boolean hasChildren =
                organizationScopeResolver.resolve(currentOrganization).mode() != OrganizationScopeMode.ORGANIZATION;

        Map<String, Form12Counts> currentByCode = countsByCode(organizationIds, from, to, 0);
        Map<String, Form12Counts> previousByCode = countsByCode(organizationIds, from, to, 1);

        List<ManualReportResponse> entries = manualReportQueryService.findByReportType(MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();

        List<Form12ReportNodeResponse> rows = entries.stream()
                .map(entry -> nosologyRow(entry, currentByCode, previousByCode, hasChildren))
                .collect(Collectors.toCollection(ArrayList::new));

        rows.add(totalRow(entries, currentByCode, previousByCode));
        return rows;
    }

    public List<Form12ReportNodeResponse> getChildren(
            Long manualReportId, String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        ManualReportResponse entry = manualReportQueryService.getById(manualReportId);
        Set<String> codes = normalizeCodes(entry.icd10Codes());
        Form12GeographyCountSource countSource = new Form12GeographyCountSource(form12ReportRepository, codes);

        List<ReportHierarchyNode<Form12Counts>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form12Counts>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to, 1), null
        );

        Map<String, Form12Counts> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> row(
                        node.code(), node.name(), null, null, node.hasChildren(),
                        previousByCode.getOrDefault(node.code(), Form12Counts.EMPTY), node.counts()
                ))
                .toList();
    }

    private Map<String, Form12Counts> countsByCode(List<Long> organizationIds, LocalDate from, LocalDate to,
                                                   long yearsAgo) {
        ReportDateRange range = reportDateRangeResolver.resolve(from, to, yearsAgo);
        return form12ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())
                .stream()
                .collect(Collectors.toMap(
                        p -> p.code().toUpperCase(Locale.ROOT),
                        p -> new Form12Counts(p.total(), p.under14(), p.under18()),
                        Form12Counts::plus
                ));
    }

    private Form12ReportNodeResponse nosologyRow(
            ManualReportResponse entry,
            Map<String, Form12Counts> currentByCode,
            Map<String, Form12Counts> previousByCode,
            boolean hasChildren
    ) {
        return row(
                String.valueOf(entry.id()), localizedName(entry), entry.code(), entry.shortName(), hasChildren,
                rollUp(entry, previousByCode), rollUp(entry, currentByCode)
        );
    }

    private Form12ReportNodeResponse totalRow(
            List<ManualReportResponse> entries,
            Map<String, Form12Counts> currentByCode,
            Map<String, Form12Counts> previousByCode
    ) {
        List<ManualReportResponse> included = entries.stream()
                .filter(e -> Boolean.TRUE.equals(e.includeInTotal()))
                .toList();

        Form12Counts current = included.stream()
                .map(e -> rollUp(e, currentByCode)).reduce(Form12Counts.EMPTY, Form12Counts::plus);
        Form12Counts previous = included.stream()
                .map(e -> rollUp(e, previousByCode)).reduce(Form12Counts.EMPTY, Form12Counts::plus);

        return row(TOTAL_CODE, messageResolver.resolve("report.scope.total"), null, null, false, previous, current);
    }

    /**
     * Sum the per-code counts for every ICD-10 code in one catalog entry's set.
     */
    private Form12Counts rollUp(ManualReportResponse entry, Map<String, Form12Counts> countsByCode) {
        return normalizeCodes(entry.icd10Codes()).stream()
                .map(code -> countsByCode.getOrDefault(code, Form12Counts.EMPTY))
                .reduce(Form12Counts.EMPTY, Form12Counts::plus);
    }

    private Form12ReportNodeResponse row(
            String code, String name, String rowCode, String icd10Display, boolean hasChildren,
            Form12Counts previous, Form12Counts current
    ) {
        return new Form12ReportNodeResponse(
                code, name, rowCode, icd10Display, hasChildren,
                previous.total(), current.total(), current.total() - previous.total(),
                previous.under14(), current.under14(), current.under14() - previous.under14(),
                previous.under18(), current.under18(), current.under18() - previous.under18()
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
