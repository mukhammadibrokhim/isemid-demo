package uz.uzinfocom.app.modules.report.form2.manual.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form2.infrastructure.persistence.repository.Form2ReportRepository;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryFilterRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryPrefillResponse;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryTableResponse;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.mapper.Form2ManualEntryMapper;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.repository.Form2ManualEntryRepository;
import uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.specification.Form2ManualEntrySpecification;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
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
 * Prefill (the two auto-computed counts for the create form) and the
 * organization-scoped table listing for "Shakl №2" manual entries. Reuses
 * {@link Form2ReportRepository}'s existing case-counting SQL and {@link
 * ReportDateRangeResolver}'s existing "current period vs. the same calendar
 * dates one year earlier" math (the same pattern {@code
 * Form3ReportQueryService} uses) instead of re-deriving either.
 * <p>
 * "Registered cases" is every form058/form0581 primary notification created
 * by the current organization in the given period — no diagnosis filtering.
 */
@Service
@RequiredArgsConstructor
public class Form2ManualEntryQueryService {

    private final Form2ManualEntryRepository form2ManualEntryRepository;
    private final Form2ManualEntrySpecification form2ManualEntrySpecification;
    private final Form2ManualEntryMapper form2ManualEntryMapper;
    private final Form2ReportRepository form2ReportRepository;
    private final ReportDateRangeResolver reportDateRangeResolver;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    @Transactional(readOnly = true)
    public Form2ManualEntryPrefillResponse prefill(LocalDate from, LocalDate to) {
        Organization currentOrganization = requireCurrentOrganization();
        RegisteredCaseCounts counts = registeredCaseCounts(currentOrganization.getId(), from, to);

        String regionCode = currentOrganization.getRegionCode();
        String districtCode = currentOrganization.getDistrictCode();

        return new Form2ManualEntryPrefillResponse(
                from,
                to,
                currentOrganization.getId(),
                organizationNameResolver.resolve(currentOrganization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                counts.lastYear(),
                counts.currentYear()
        );
    }

    @Transactional(readOnly = true)
    public Page<Form2ManualEntryTableResponse> findTable(Form2ManualEntryFilterRequest request) {
        Form2ManualEntryFilterRequest filter = request == null
                ? new Form2ManualEntryFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Form2ManualEntrySortFields.ALLOWED_SORT_FIELDS);
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());

        Page<Form2ManualEntry> page = form2ManualEntryRepository.findAll(
                form2ManualEntrySpecification.byFilter(filter, scope), pageable
        );

        Map<Long, Organization> organizationsById = organizationRepository
                .findAllById(page.getContent().stream().map(Form2ManualEntry::getOrganizationId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return page.map(entity -> form2ManualEntryMapper.toTableResponse(
                entity, organizationsById.get(entity.getOrganizationId())
        ));
    }

    /**
     * Computes both the current-period and same-calendar-dates-one-year-earlier
     * registered case counts for a single organization. Public so {@code
     * Form2ManualEntryCommandService} can re-derive the same snapshot
     * server-side at save time instead of trusting client-supplied numbers.
     */
    public RegisteredCaseCounts registeredCaseCounts(Long organizationId, LocalDate from, LocalDate to) {
        List<Long> organizationIds = List.of(organizationId);

        long currentYear = countTotal(organizationIds, from, to, 0);
        long lastYear = countTotal(organizationIds, from, to, 1);

        return new RegisteredCaseCounts(lastYear, currentYear);
    }

    private long countTotal(List<Long> organizationIds, LocalDate from, LocalDate to, long yearsAgo) {
        ReportDateRange range = reportDateRangeResolver.resolve(from, to, yearsAgo);
        return form2ReportRepository
                .countTotal(organizationIds, range.fromInclusive(), range.toExclusive())
                .total();
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }

    public record RegisteredCaseCounts(long lastYear, long currentYear) {
    }
}
