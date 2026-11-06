package uz.uzinfocom.app.modules.report.form32.application.export;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.form32.application.query.Form32EntrySortFields;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryTableResponse;
import uz.uzinfocom.app.modules.report.form32.application.query.mapper.Form32EntryMapper;
import uz.uzinfocom.app.modules.report.form32.domain.Form32Entry;
import uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.repository.Form32EntryRepository;
import uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.specification.Form32EntrySpecification;
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
 * Excel export for "Shakl №3-2" (sanitary inspection) manual entries —
 * mirrors {@code Form31EntryExcelExportSource} exactly.
 */
@Component
@RequiredArgsConstructor
public class Form32EntryExcelExportSource
        implements ExcelExportSource<Form32EntryFilterRequest, Form32EntryTableResponse> {

    private final Form32EntryRepository repository;
    private final Form32EntrySpecification specification;
    private final Form32EntryMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final MessageResolver messageResolver;

    @Override
    public String exportType() {
        return "FORM32";
    }

    @Override
    @Transactional(readOnly = true)
    public long count(Form32EntryFilterRequest filter) {
        return repository.count(resolveSpecification(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public void forEachRow(
            Form32EntryFilterRequest filter,
            Consumer<Form32EntryTableResponse> rowProcessor
    ) {
        Specification<Form32Entry> spec = resolveSpecification(filter);
        Sort sort = PageableUtils.of(filter, Form32EntrySortFields.ALLOWED_SORT_FIELDS).getSort();
        Map<Long, Organization> organizationCache = new HashMap<>();

        try (Stream<Form32Entry> entities = repository.findBy(spec, query -> query.sortBy(sort).stream())) {
            entities.forEach(entity -> rowProcessor.accept(
                    mapper.toTableResponse(entity, resolveOrganization(entity.getOrganizationId(), organizationCache))
            ));
        }
    }

    @Override
    public List<ExcelColumn<Form32EntryTableResponse>> availableColumns() {
        return List.of(
                ExcelColumn.of("id", messageResolver.resolve("report.export.shared.id"), Form32EntryTableResponse::id),
                ExcelColumn.of("organizationName", messageResolver.resolve("report.export.shared.organizationName"), Form32EntryTableResponse::organizationName),
                ExcelColumn.of("regionName", messageResolver.resolve("report.export.shared.regionName"), Form32EntryTableResponse::regionName),
                ExcelColumn.of("districtName", messageResolver.resolve("report.export.shared.districtName"), Form32EntryTableResponse::districtName),
                ExcelColumn.of("fromDate", messageResolver.resolve("report.export.shared.fromDate"), Form32EntryTableResponse::fromDate),
                ExcelColumn.of("toDate", messageResolver.resolve("report.export.shared.toDate"), Form32EntryTableResponse::toDate),
                ExcelColumn.of("inspectedTotalCount", messageResolver.resolve("report.form32.export.inspectedTotalCount"), Form32EntryTableResponse::inspectedTotalCount),
                ExcelColumn.of("inspectedMtmCount", messageResolver.resolve("report.form32.export.inspectedMtmCount"), Form32EntryTableResponse::inspectedMtmCount),
                ExcelColumn.of("inspectedSchoolCount", messageResolver.resolve("report.form32.export.inspectedSchoolCount"), Form32EntryTableResponse::inspectedSchoolCount),
                ExcelColumn.of("inspectedDpmCount", messageResolver.resolve("report.form32.export.inspectedDpmCount"), Form32EntryTableResponse::inspectedDpmCount),
                ExcelColumn.of("inspectedOtherCount", messageResolver.resolve("report.form32.export.inspectedOtherCount"), Form32EntryTableResponse::inspectedOtherCount),
                ExcelColumn.of("deficiencyTotalCount", messageResolver.resolve("report.form32.export.deficiencyTotalCount"), Form32EntryTableResponse::deficiencyTotalCount),
                ExcelColumn.of("deficiencyMtmCount", messageResolver.resolve("report.form32.export.deficiencyMtmCount"), Form32EntryTableResponse::deficiencyMtmCount),
                ExcelColumn.of("deficiencySchoolCount", messageResolver.resolve("report.form32.export.deficiencySchoolCount"), Form32EntryTableResponse::deficiencySchoolCount),
                ExcelColumn.of("deficiencyDpmCount", messageResolver.resolve("report.form32.export.deficiencyDpmCount"), Form32EntryTableResponse::deficiencyDpmCount),
                ExcelColumn.of("deficiencyOtherCount", messageResolver.resolve("report.form32.export.deficiencyOtherCount"), Form32EntryTableResponse::deficiencyOtherCount),
                ExcelColumn.of("officialTotalCount", messageResolver.resolve("report.form32.export.officialTotalCount"), Form32EntryTableResponse::officialTotalCount),
                ExcelColumn.of("officialMtmCount", messageResolver.resolve("report.form32.export.officialMtmCount"), Form32EntryTableResponse::officialMtmCount),
                ExcelColumn.of("officialSchoolCount", messageResolver.resolve("report.form32.export.officialSchoolCount"), Form32EntryTableResponse::officialSchoolCount),
                ExcelColumn.of("officialDpmCount", messageResolver.resolve("report.form32.export.officialDpmCount"), Form32EntryTableResponse::officialDpmCount),
                ExcelColumn.of("officialOtherCount", messageResolver.resolve("report.form32.export.officialOtherCount"), Form32EntryTableResponse::officialOtherCount),
                ExcelColumn.of("suspendedTotalCount", messageResolver.resolve("report.form32.export.suspendedTotalCount"), Form32EntryTableResponse::suspendedTotalCount),
                ExcelColumn.of("suspendedMtmCount", messageResolver.resolve("report.form32.export.suspendedMtmCount"), Form32EntryTableResponse::suspendedMtmCount),
                ExcelColumn.of("suspendedSchoolCount", messageResolver.resolve("report.form32.export.suspendedSchoolCount"), Form32EntryTableResponse::suspendedSchoolCount),
                ExcelColumn.of("suspendedDpmCount", messageResolver.resolve("report.form32.export.suspendedDpmCount"), Form32EntryTableResponse::suspendedDpmCount),
                ExcelColumn.of("suspendedOtherCount", messageResolver.resolve("report.form32.export.suspendedOtherCount"), Form32EntryTableResponse::suspendedOtherCount),
                ExcelColumn.of("createdAt", messageResolver.resolve("report.export.shared.createdAt"), Form32EntryTableResponse::createdAt)
        );
    }

    @Override
    public String sheetName() {
        return "Shakl 3-2";
    }

    @Override
    public String fileNamePrefix() {
        return "form32_export";
    }

    private Organization resolveOrganization(Long organizationId, Map<Long, Organization> cache) {
        return cache.computeIfAbsent(organizationId, id -> organizationRepository.findById(id).orElse(null));
    }

    private Specification<Form32Entry> resolveSpecification(Form32EntryFilterRequest filter) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());
        return specification.byFilter(filter, scope);
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
