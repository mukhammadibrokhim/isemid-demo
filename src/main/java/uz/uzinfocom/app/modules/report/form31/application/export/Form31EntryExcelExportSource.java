package uz.uzinfocom.app.modules.report.form31.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.form31.application.query.Form31EntrySortFields;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryTableResponse;
import uz.uzinfocom.app.modules.report.form31.application.query.mapper.Form31EntryMapper;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;
import uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.repository.Form31EntryRepository;
import uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.specification.Form31EntrySpecification;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Excel export for "Shakl №3-1" (ILI/SARI surveillance) manual entries —
 * mirrors {@code Form2ManualEntryExcelExportSource} exactly (same
 * organization-scoped specification, same per-export organization cache);
 * this module just has no auto-computed prefill fields.
 */
@Component
@RequiredArgsConstructor
public class Form31EntryExcelExportSource
        implements ExcelExportSource<Form31EntryFilterRequest, Form31EntryTableResponse> {

    private final Form31EntryRepository repository;
    private final Form31EntrySpecification specification;
    private final Form31EntryMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM31";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form31EntryFilterRequest filter) {
        return repository.count(resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(
            Form31EntryFilterRequest filter,
            Consumer<Form31EntryTableResponse> rowProcessor
    ) {
        Specification<Form31Entry> spec = resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form31EntrySortFields.ALLOWED_SORT_FIELDS).getSort();
        Map<Long, Organization> organizationCache = new HashMap<>();

        try (Stream<Form31Entry> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> rowProcessor.accept(
                    mapper.toTableResponse(entity, resolveOrganization(entity.getOrganizationId(), organizationCache))
            ));
        }
    }

    @Override
    public List<ExcelColumn<Form31EntryTableResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", messageResolver.resolve("report.export.shared.id"), Form31EntryTableResponse::id),
                ExcelColumn.of("organizationName", messageResolver.resolve("report.export.shared.organizationName"), Form31EntryTableResponse::organizationName),
                ExcelColumn.of("regionName", messageResolver.resolve("report.export.shared.regionName"), Form31EntryTableResponse::regionName),
                ExcelColumn.of("districtName", messageResolver.resolve("report.export.shared.districtName"), Form31EntryTableResponse::districtName),
                ExcelColumn.of("fromDate", messageResolver.resolve("report.export.shared.fromDate"), Form31EntryTableResponse::fromDate),
                ExcelColumn.of("toDate", messageResolver.resolve("report.export.shared.toDate"), Form31EntryTableResponse::toDate),
                ExcelColumn.of("iliCasesCount", messageResolver.resolve("report.form31.export.iliCasesCount"), Form31EntryTableResponse::iliCasesCount),
                ExcelColumn.of("ariCasesCount", messageResolver.resolve("report.form31.export.ariCasesCount"), Form31EntryTableResponse::ariCasesCount),
                ExcelColumn.of("pneumoniaCasesCount", messageResolver.resolve("report.form31.export.pneumoniaCasesCount"), Form31EntryTableResponse::pneumoniaCasesCount),
                ExcelColumn.of("sariTotalCount", messageResolver.resolve("report.form31.export.sariTotalCount"), Form31EntryTableResponse::sariTotalCount),
                ExcelColumn.of("sariPregnantCount", messageResolver.resolve("report.form31.export.sariPregnantCount"), Form31EntryTableResponse::sariPregnantCount),
                ExcelColumn.of("deathTotalCount", messageResolver.resolve("report.form31.export.deathTotalCount"), Form31EntryTableResponse::deathTotalCount),
                ExcelColumn.of("deathPregnantCount", messageResolver.resolve("report.form31.export.deathPregnantCount"), Form31EntryTableResponse::deathPregnantCount),
                ExcelColumn.of("weeklyVaccinationCount", messageResolver.resolve("report.form31.export.weeklyVaccinationCount"), Form31EntryTableResponse::weeklyVaccinationCount),
                ExcelColumn.of("seasonVaccinationCount", messageResolver.resolve("report.form31.export.seasonVaccinationCount"), Form31EntryTableResponse::seasonVaccinationCount),
                ExcelColumn.of("createdAt", messageResolver.resolve("report.export.shared.createdAt"), Form31EntryTableResponse::createdAt)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 3-1";
    }

    @Override
    public String fileNamePrefix() {
        return "form31_export";
    }

    private Organization resolveOrganization(Long organizationId, Map<Long, Organization> cache) {
        return cache.computeIfAbsent(organizationId, id -> organizationRepository.findById(id).orElse(null));
    }

    private Specification<Form31Entry> resolveSpecification(Form31EntryFilterRequest filter) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        return specification.byFilter(filter, scope);
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
