package uz.uzinfocom.app.modules.report.analytic.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.analytic.application.query.AnalyticReportSortFields;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportFilterRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.mapper.AnalyticReportMapper;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.AnalyticReportRepository;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.specification.AnalyticReportSpecification;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.export.application.ExcelExportSource;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.excel.ExcelColumn;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Excel export for "Analitik hisobot" — same organization-scoped {@link
 * AnalyticReportSpecification} the paged {@code findTable} listing already
 * uses, streamed instead of paged. Uses {@link AnalyticReportMapper#toResponse}
 * (not {@code toTableResponse}) so the saved {@code content} rich-text field
 * — otherwise only available one record at a time via {@code GET /{id}} —
 * is included as one wide text column, per this module's own convention that
 * {@code content} is opaque, caller-authored text.
 */
@Component
@RequiredArgsConstructor
public class AnalyticReportExcelExportSource
        implements ExcelExportSource<AnalyticReportFilterRequest, AnalyticReportResponse> {

    private final AnalyticReportRepository repository;
    private final AnalyticReportSpecification specification;
    private final AnalyticReportMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "ANALYTIC_REPORT";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(AnalyticReportFilterRequest filter) {
        return repository.count(resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(AnalyticReportFilterRequest filter, Consumer<AnalyticReportResponse> rowProcessor) {
        Specification<AnalyticReport> spec = resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, AnalyticReportSortFields.ALLOWED_SORT_FIELDS).getSort();
        Map<Long, Organization> organizationCache = new HashMap<>();

        try (Stream<AnalyticReport> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> rowProcessor.accept(
                    mapper.toResponse(entity, resolveOrganization(entity.getOrganizationId(), organizationCache))
            ));
        }
    }

    @Override
    public List<ExcelColumn<AnalyticReportResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", messageResolver.resolve("report.export.shared.id"), AnalyticReportResponse::id),
                ExcelColumn.of("name", messageResolver.resolve("report.analytic_report.export.name"), AnalyticReportResponse::name),
                ExcelColumn.of("status", messageResolver.resolve("report.analytic_report.export.status"), AnalyticReportResponse::status),
                ExcelColumn.of("fromDate", messageResolver.resolve("report.export.shared.fromDate"), AnalyticReportResponse::fromDate),
                ExcelColumn.of("toDate", messageResolver.resolve("report.export.shared.toDate"), AnalyticReportResponse::toDate),
                ExcelColumn.of("regionCodes", messageResolver.resolve("report.analytic_report.export.regionCodes"), row -> joinCodes(row.regionCodes())),
                ExcelColumn.of("icd10Codes", messageResolver.resolve("report.analytic_report.export.icd10Codes"), row -> joinCodes(row.icd10Codes())),
                ExcelColumn.of("koef", messageResolver.resolve("report.analytic_report.export.koef"), AnalyticReportResponse::koef),
                ExcelColumn.of("organizationName", messageResolver.resolve("report.export.shared.organizationName"), AnalyticReportResponse::organizationName),
                ExcelColumn.of("createdAt", messageResolver.resolve("report.export.shared.createdAt"), AnalyticReportResponse::createdAt),
                ExcelColumn.of("updatedAt", messageResolver.resolve("report.export.shared.updatedAt"), AnalyticReportResponse::updatedAt),
                ExcelColumn.of("content", messageResolver.resolve("report.analytic_report.export.content"), AnalyticReportResponse::content)
        );
    }

    @Override
    public String sheetName() {
        return "Analitik hisobot";
    }

    @Override
    public String fileNamePrefix() {
        return "analytic_report_export";
    }

    private String joinCodes(Set<String> codes) {
        return codes == null || codes.isEmpty() ? null : String.join(", ", codes);
    }

    private Organization resolveOrganization(Long organizationId, Map<Long, Organization> cache) {
        return cache.computeIfAbsent(organizationId, id -> organizationRepository.findById(id).orElse(null));
    }

    private Specification<AnalyticReport> resolveSpecification(AnalyticReportFilterRequest filter) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        return specification.byFilter(filter, scope);
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
