package uz.uzinfocom.app.modules.report.form13.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13ByDiseaseReportNodeResponse;
import uz.uzinfocom.app.modules.report.form13.application.query.dto.Form13Metric;
import uz.uzinfocom.app.modules.report.form13.infrastructure.persistence.repository.Form13ReportRepository;
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
 * "Form 13 by disease" — the disease-first counterpart of "Form 13", the same
 * relationship "Form 12" has to "Form 13" itself: unlike the geography-first
 * "Form 13", this view's root level is a flat list of <b>nosological forms</b>
 * — every {@link ManualReportResponse} tagged {@value #MANUAL_REPORT_TYPE},
 * structurally identical to {@code Form12ReportQueryService}. Each row's
 * numbers are confirmed ({@code status = 'APPROVED'}) {@code form058} / {@code
 * form058_1} cases whose confirmed diagnosis is in that entry's ICD-10 code
 * set — {@code total}, {@code under14}, {@code under18}, each shown for the
 * chosen period next to the same calendar dates one year earlier and the
 * delta.
 * <p>
 * Expanding a row drills into geography (republic→region→district→organization),
 * one level per call, for that one nosological form — delegated to the shared
 * {@link ReportHierarchyService} with a per-request {@link
 * Form13ByDiseaseGeographyCountSource}.
 */
@Service
@RequiredArgsConstructor
public class Form13ByDiseaseReportQueryService {

    /** The {@code ManualReport.reportTypes} tag a catalog entry must carry to appear in this view. */
    public static final String MANUAL_REPORT_TYPE = Form13ReportQueryService.MANUAL_REPORT_TYPE;

    private static final String TOTAL_CODE = "TOTAL";

    private final Form13ReportRepository form13ReportRepository;
    private final ManualReportQueryService manualReportQueryService;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final LocalizedTextResolver localizedTextResolver;
    private final MessageResolver messageResolver;

    public List<Form13ByDiseaseReportNodeResponse> getRoot(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();

        ResolvedReportNode scopeNode = reportHierarchyService.resolveNode(currentOrganization, null, null);
        List<Long> organizationIds = scopeNode.organizationIds();
        boolean hasChildren =
                organizationScopeResolver.resolve(currentOrganization).mode() != OrganizationScopeMode.ORGANIZATION;

        Map<String, Form13Metric> currentByCode = countsByCode(organizationIds, from, to, 0);
        Map<String, Form13Metric> previousByCode = countsByCode(organizationIds, from, to, 1);

        List<ManualReportResponse> entries = manualReportQueryService.findByReportType(MANUAL_REPORT_TYPE).stream()
                .sorted((a, b) -> nullSafe(a.code()).compareToIgnoreCase(nullSafe(b.code())))
                .toList();

        List<Form13ByDiseaseReportNodeResponse> rows = entries.stream()
                .map(entry -> nosologyRow(entry, currentByCode, previousByCode, hasChildren))
                .collect(Collectors.toCollection(ArrayList::new));

        rows.add(totalRow(entries, currentByCode, previousByCode));
        return rows;
    }

    public List<Form13ByDiseaseReportNodeResponse> getChildren(
            Long manualReportId, String regionCode, String districtCode, LocalDate from, LocalDate to
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        ManualReportResponse entry = manualReportQueryService.getById(manualReportId);
        Set<String> codes = normalizeCodes(entry.icd10Codes());
        Form13ByDiseaseGeographyCountSource countSource =
                new Form13ByDiseaseGeographyCountSource(form13ReportRepository, codes);

        List<ReportHierarchyNode<Form13Metric>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to), null
        );
        List<ReportHierarchyNode<Form13Metric>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, countSource,
                reportDateRangeResolver.resolve(from, to, 1), null
        );

        Map<String, Form13Metric> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> row(
                        node.code(), node.name(), null, null, node.hasChildren(),
                        previousByCode.getOrDefault(node.code(), Form13Metric.EMPTY), node.counts()
                ))
                .toList();
    }

    private Map<String, Form13Metric> countsByCode(List<Long> organizationIds, LocalDate from, LocalDate to,
                                                     long yearsAgo) {
        ReportDateRange range = reportDateRangeResolver.resolve(from, to, yearsAgo);
        return form13ReportRepository
                .countByDiagnosisCode(organizationIds, range.fromInclusive(), range.toExclusive())
                .stream()
                .collect(Collectors.toMap(
                        p -> p.code().toUpperCase(Locale.ROOT),
                        p -> new Form13Metric(p.total(), p.under14(), p.under18()),
                        Form13Metric::plus
                ));
    }

    private Form13ByDiseaseReportNodeResponse nosologyRow(
            ManualReportResponse entry,
            Map<String, Form13Metric> currentByCode,
            Map<String, Form13Metric> previousByCode,
            boolean hasChildren
    ) {
        return row(
                String.valueOf(entry.id()), localizedName(entry), entry.code(), entry.shortName(), hasChildren,
                rollUp(entry, previousByCode), rollUp(entry, currentByCode)
        );
    }

    private Form13ByDiseaseReportNodeResponse totalRow(
            List<ManualReportResponse> entries,
            Map<String, Form13Metric> currentByCode,
            Map<String, Form13Metric> previousByCode
    ) {
        List<ManualReportResponse> included = entries.stream()
                .filter(e -> Boolean.TRUE.equals(e.includeInTotal()))
                .toList();

        Form13Metric current = included.stream()
                .map(e -> rollUp(e, currentByCode)).reduce(Form13Metric.EMPTY, Form13Metric::plus);
        Form13Metric previous = included.stream()
                .map(e -> rollUp(e, previousByCode)).reduce(Form13Metric.EMPTY, Form13Metric::plus);

        return row(TOTAL_CODE, messageResolver.resolve("report.scope.total"), null, null, false, previous, current);
    }

    /**
     * Sum the per-code counts for every ICD-10 code in one catalog entry's set.
     */
    private Form13Metric rollUp(ManualReportResponse entry, Map<String, Form13Metric> countsByCode) {
        return normalizeCodes(entry.icd10Codes()).stream()
                .map(code -> countsByCode.getOrDefault(code, Form13Metric.EMPTY))
                .reduce(Form13Metric.EMPTY, Form13Metric::plus);
    }

    private Form13ByDiseaseReportNodeResponse row(
            String code, String name, String rowCode, String icd10Display, boolean hasChildren,
            Form13Metric previous, Form13Metric current
    ) {
        return new Form13ByDiseaseReportNodeResponse(
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
