package uz.uzinfocom.app.modules.report.form7.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7CaseCountProjection;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryPrefillResponse;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryTableResponse;
import uz.uzinfocom.app.modules.report.form7.application.query.mapper.Form7EntryMapper;
import uz.uzinfocom.app.modules.report.form7.domain.Form7Entry;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository.Form7CaseCountRepository;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository.Form7EntryRepository;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.specification.Form7EntrySpecification;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Prefill (the auto-computed «Hisobot davrida ro'yxatga olingan bemorlar»
 * block + «Birlamchi tashxis tasdiqlandi» for the create form) and the
 * organization-scoped table listing for "Shakl №7" manual entries. Reuses
 * {@link Form7CaseCountRepository}'s native case-counting SQL and {@link
 * ReportDateRangeResolver}'s existing "no date given" math instead of
 * re-deriving either — same pattern as {@code Form2ManualEntryQueryService}.
 */
@Service
@RequiredArgsConstructor
public class Form7EntryQueryService {

    private final Form7EntryRepository form7EntryRepository;
    private final Form7EntrySpecification form7EntrySpecification;
    private final Form7EntryMapper form7EntryMapper;
    private final Form7CaseCountRepository form7CaseCountRepository;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    @Transactional(readOnly = true)
    public Form7EntryPrefillResponse prefill(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();
        Form7CaseCountProjection counts = caseCounts(currentOrganization.getId(), from, to);

        String regionCode = currentOrganization.getRegionCode();
        String districtCode = currentOrganization.getDistrictCode();

        return new Form7EntryPrefillResponse(
                from,
                to,
                currentOrganization.getId(),
                organizationNameResolver.resolve(currentOrganization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                counts.total(),
                counts.under14(),
                counts.under18(),
                counts.adult(),
                counts.female(),
                counts.primaryDiagnosisConfirmed()
        );
    }

    @Transactional(readOnly = true)
    public Page<Form7EntryTableResponse> findTable(Form7EntryFilterRequest request) {
        Form7EntryFilterRequest filter = request == null
                ? new Form7EntryFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Form7EntrySortFields.ALLOWED_SORT_FIELDS);
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());

        Page<Form7Entry> page = form7EntryRepository.findAll(
                form7EntrySpecification.byFilter(filter, scope), pageable
        );

        Map<Long, Organization> organizationsById = organizationRepository
                .findAllById(page.getContent().stream().map(Form7Entry::getOrganizationId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return page.map(entity -> form7EntryMapper.toTableResponse(
                entity, organizationsById.get(entity.getOrganizationId())
        ));
    }

    /**
     * Computes the auto-derivable Form 7 count block for a single
     * organization and period. Public so {@code Form7EntryCommandService}
     * can re-derive the same snapshot server-side at save time instead of
     * trusting client-supplied numbers.
     */
    public Form7CaseCountProjection caseCounts(Long organizationId, LocalDate from, LocalDate to) {
        ReportDateRange range = reportDateRangeResolver.resolve(from, to);
        return form7CaseCountRepository.countBlock(
                List.of(organizationId), range.fromInclusive(), range.toExclusive()
        );
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
