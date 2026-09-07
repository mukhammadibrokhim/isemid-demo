package uz.uzinfocom.app.modules.report.form2.manual.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.Form2ManualEntrySortFields;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryFilterRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryTableResponse;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.mapper.Form2ManualEntryMapper;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.repository.Form2ManualEntryRepository;
import uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.specification.Form2ManualEntrySpecification;
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
 * Excel export for "Shakl №2" manual entries — same organization-scoped
 * {@link Form2ManualEntrySpecification} the paged {@code findTable} listing
 * already uses, streamed instead of paged. Mirrors {@code
 * Form058ExcelExportSource}'s shape; the only extra step is resolving each
 * row's creating {@link Organization} (a plain scalar {@code organizationId}
 * on the entity, not a JPA association) through a small per-export cache,
 * since the same organization typically repeats across many rows of one
 * export.
 */
@Component
@RequiredArgsConstructor
public class Form2ManualEntryExcelExportSource
        implements ExcelExportSource<Form2ManualEntryFilterRequest, Form2ManualEntryTableResponse> {

    private final Form2ManualEntryRepository repository;
    private final Form2ManualEntrySpecification specification;
    private final Form2ManualEntryMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM2_MANUAL";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form2ManualEntryFilterRequest filter) {
        return repository.count(resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(
            Form2ManualEntryFilterRequest filter,
            Consumer<Form2ManualEntryTableResponse> rowProcessor
    ) {
        Specification<Form2ManualEntry> spec = resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form2ManualEntrySortFields.ALLOWED_SORT_FIELDS).getSort();
        Map<Long, Organization> organizationCache = new HashMap<>();

        try (Stream<Form2ManualEntry> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> rowProcessor.accept(
                    mapper.toTableResponse(entity, resolveOrganization(entity.getOrganizationId(), organizationCache))
            ));
        }
    }

    @Override
    public List<ExcelColumn<Form2ManualEntryTableResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", messageResolver.resolve("report.export.shared.id"), Form2ManualEntryTableResponse::id),
                ExcelColumn.of("organizationName", messageResolver.resolve("report.export.shared.organizationName"), Form2ManualEntryTableResponse::organizationName),
                ExcelColumn.of("regionName", messageResolver.resolve("report.export.shared.regionName"), Form2ManualEntryTableResponse::regionName),
                ExcelColumn.of("districtName", messageResolver.resolve("report.export.shared.districtName"), Form2ManualEntryTableResponse::districtName),
                ExcelColumn.of("fromDate", messageResolver.resolve("report.export.shared.fromDate"), Form2ManualEntryTableResponse::fromDate),
                ExcelColumn.of("toDate", messageResolver.resolve("report.export.shared.toDate"), Form2ManualEntryTableResponse::toDate),
                ExcelColumn.of("registeredCasesLastYear", messageResolver.resolve("report.form2_manual.export.registeredCasesLastYear"), Form2ManualEntryTableResponse::registeredCasesLastYear),
                ExcelColumn.of("registeredCasesCurrentYear", messageResolver.resolve("report.form2_manual.export.registeredCasesCurrentYear"), Form2ManualEntryTableResponse::registeredCasesCurrentYear),
                ExcelColumn.of("mtmSchoolCount", messageResolver.resolve("report.form2_manual.export.mtmSchoolCount"), Form2ManualEntryTableResponse::mtmSchoolCount),
                ExcelColumn.of("disinfectionFociCount", messageResolver.resolve("report.form2_manual.export.disinfectionFociCount"), Form2ManualEntryTableResponse::disinfectionFociCount),
                ExcelColumn.of("inspectedObjectsCount", messageResolver.resolve("report.form2_manual.export.inspectedObjectsCount"), Form2ManualEntryTableResponse::inspectedObjectsCount),
                ExcelColumn.of("closedObjectsCount", messageResolver.resolve("report.form2_manual.export.closedObjectsCount"), Form2ManualEntryTableResponse::closedObjectsCount),
                ExcelColumn.of("dduCount", messageResolver.resolve("report.form2_manual.export.dduCount"), Form2ManualEntryTableResponse::dduCount),
                ExcelColumn.of("schoolCount", messageResolver.resolve("report.form2_manual.export.schoolCount"), Form2ManualEntryTableResponse::schoolCount),
                ExcelColumn.of("finesCount", messageResolver.resolve("report.form2_manual.export.finesCount"), Form2ManualEntryTableResponse::finesCount),
                ExcelColumn.of("prosecutorReferralCount", messageResolver.resolve("report.form2_manual.export.prosecutorReferralCount"), Form2ManualEntryTableResponse::prosecutorReferralCount),
                ExcelColumn.of("createdAt", messageResolver.resolve("report.export.shared.createdAt"), Form2ManualEntryTableResponse::createdAt)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 2";
    }

    @Override
    public String fileNamePrefix() {
        return "form2_manual_export";
    }

    private Organization resolveOrganization(Long organizationId, Map<Long, Organization> cache) {
        return cache.computeIfAbsent(organizationId, id -> organizationRepository.findById(id).orElse(null));
    }

    private Specification<Form2ManualEntry> resolveSpecification(Form2ManualEntryFilterRequest filter) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        return specification.byFilter(filter, scope);
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
