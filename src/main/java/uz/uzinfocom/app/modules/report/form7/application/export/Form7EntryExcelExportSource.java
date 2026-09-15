package uz.uzinfocom.app.modules.report.form7.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.form7.application.query.Form7EntrySortFields;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryTableResponse;
import uz.uzinfocom.app.modules.report.form7.application.query.mapper.Form7EntryMapper;
import uz.uzinfocom.app.modules.report.form7.domain.Form7Entry;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository.Form7EntryRepository;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.specification.Form7EntrySpecification;
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
 * Excel export for "Shakl №7" manual entries — mirrors {@code
 * Form2ManualEntryExcelExportSource}; the auto-computed age/gender snapshot
 * and {@code caseChange} are already resolved fields on {@link
 * Form7EntryTableResponse} via {@link Form7EntryMapper}, so no extra work is
 * needed here beyond the usual per-row organization lookup.
 */
@Component
@RequiredArgsConstructor
public class Form7EntryExcelExportSource
        implements ExcelExportSource<Form7EntryFilterRequest, Form7EntryTableResponse> {

    private final Form7EntryRepository repository;
    private final Form7EntrySpecification specification;
    private final Form7EntryMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM7";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form7EntryFilterRequest filter) {
        return repository.count(resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(
            Form7EntryFilterRequest filter,
            Consumer<Form7EntryTableResponse> rowProcessor
    ) {
        Specification<Form7Entry> spec = resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form7EntrySortFields.ALLOWED_SORT_FIELDS).getSort();
        Map<Long, Organization> organizationCache = new HashMap<>();

        try (Stream<Form7Entry> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> rowProcessor.accept(
                    mapper.toTableResponse(entity, resolveOrganization(entity.getOrganizationId(), organizationCache))
            ));
        }
    }

    @Override
    public List<ExcelColumn<Form7EntryTableResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", messageResolver.resolve("report.export.shared.id"), Form7EntryTableResponse::id),
                ExcelColumn.of("organizationName", messageResolver.resolve("report.export.shared.organizationName"), Form7EntryTableResponse::organizationName),
                ExcelColumn.of("regionName", messageResolver.resolve("report.export.shared.regionName"), Form7EntryTableResponse::regionName),
                ExcelColumn.of("districtName", messageResolver.resolve("report.export.shared.districtName"), Form7EntryTableResponse::districtName),
                ExcelColumn.of("fromDate", messageResolver.resolve("report.export.shared.fromDate"), Form7EntryTableResponse::fromDate),
                ExcelColumn.of("toDate", messageResolver.resolve("report.export.shared.toDate"), Form7EntryTableResponse::toDate),
                ExcelColumn.of("casesAtPeriodStart", messageResolver.resolve("report.form7.export.casesAtPeriodStart"), Form7EntryTableResponse::casesAtPeriodStart),
                ExcelColumn.of("registeredTotal", messageResolver.resolve("report.form7.export.registeredTotal"), Form7EntryTableResponse::registeredTotal),
                ExcelColumn.of("registeredUnder14", messageResolver.resolve("report.form7.export.registeredUnder14"), Form7EntryTableResponse::registeredUnder14),
                ExcelColumn.of("registeredUnder18", messageResolver.resolve("report.form7.export.registeredUnder18"), Form7EntryTableResponse::registeredUnder18),
                ExcelColumn.of("registeredAdult", messageResolver.resolve("report.form7.export.registeredAdult"), Form7EntryTableResponse::registeredAdult),
                ExcelColumn.of("registeredFemale", messageResolver.resolve("report.form7.export.registeredFemale"), Form7EntryTableResponse::registeredFemale),
                ExcelColumn.of("registeredUrbanCount", messageResolver.resolve("report.form7.export.registeredUrbanCount"), Form7EntryTableResponse::registeredUrbanCount),
                ExcelColumn.of("registeredRuralCount", messageResolver.resolve("report.form7.export.registeredRuralCount"), Form7EntryTableResponse::registeredRuralCount),
                ExcelColumn.of("examinedCount", messageResolver.resolve("report.form7.export.examinedCount"), Form7EntryTableResponse::examinedCount),
                ExcelColumn.of("toBeExaminedCount", messageResolver.resolve("report.form7.export.toBeExaminedCount"), Form7EntryTableResponse::toBeExaminedCount),
                ExcelColumn.of("primaryDiagnosisConfirmed", messageResolver.resolve("report.form7.export.primaryDiagnosisConfirmed"), Form7EntryTableResponse::primaryDiagnosisConfirmed),
                ExcelColumn.of("hospitalizedCount", messageResolver.resolve("report.form7.export.hospitalizedCount"), Form7EntryTableResponse::hospitalizedCount),
                ExcelColumn.of("casesAtPeriodEnd", messageResolver.resolve("report.form7.export.casesAtPeriodEnd"), Form7EntryTableResponse::casesAtPeriodEnd),
                ExcelColumn.of("caseChange", messageResolver.resolve("report.form7.export.caseChange"), Form7EntryTableResponse::caseChange),
                ExcelColumn.of("createdAt", messageResolver.resolve("report.export.shared.createdAt"), Form7EntryTableResponse::createdAt)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 7";
    }

    @Override
    public String fileNamePrefix() {
        return "form7_export";
    }

    private Organization resolveOrganization(Long organizationId, Map<Long, Organization> cache) {
        return cache.computeIfAbsent(organizationId, id -> organizationRepository.findById(id).orElse(null));
    }

    private Specification<Form7Entry> resolveSpecification(Form7EntryFilterRequest filter) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        return specification.byFilter(filter, scope);
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
