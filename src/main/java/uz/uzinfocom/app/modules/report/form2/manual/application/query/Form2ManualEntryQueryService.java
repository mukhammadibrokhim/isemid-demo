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
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.ManualReport;
import uz.uzinfocom.app.platform.reference.repository.ManualReportRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Prefill (the two auto-computed counts for the create form) and the
 * organization-scoped table listing for "Shakl №2" manual entries. Reuses
 * {@link Form2ReportRepository}'s existing case-counting SQL and {@link
 * ReportDateRangeResolver}'s existing "current period vs. the same calendar
 * dates one year earlier" math (the same pattern {@code
 * Form3ReportQueryService} uses) instead of re-deriving either.
 * <p>
 * "Registered cases" is not every form058/form0581 primary notification —
 * only the ones whose diagnosis falls under a {@link ManualReport} tagged
 * with {@link #MANUAL_REPORT_TYPE_TAG}. An administrator groups whichever
 * MKB-10 codes belong to "Shakl №2" into one or more {@code ManualReport}
 * entries (see {@code ManualReportController}) and tags each with {@code
 * "FORM2"} in {@code reportTypes}; this class unions all of their {@code
 * mkb10Codes} at read time. No {@code ManualReport} tagged this way yet
 * means zero, not "count everything".
 */
@Service
@RequiredArgsConstructor
public class Form2ManualEntryQueryService {

    /** {@code ManualReport.reportTypes} tag whose {@code mkb10Codes} count toward Shakl №2. */
    public static final String MANUAL_REPORT_TYPE_TAG = "FORM2";

    private final Form2ManualEntryRepository form2ManualEntryRepository;
    private final Form2ManualEntrySpecification form2ManualEntrySpecification;
    private final Form2ManualEntryMapper form2ManualEntryMapper;
    private final Form2ReportRepository form2ReportRepository;
    private final ManualReportRepository manualReportRepository;
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
        Set<String> mkb10Codes = manualReportMkb10Codes();

        long currentYear = countTotal(organizationIds, mkb10Codes, from, to, 0);
        long lastYear = countTotal(organizationIds, mkb10Codes, from, to, 1);

        return new RegisteredCaseCounts(lastYear, currentYear);
    }

    /** Unions {@code mkb10Codes} across every non-deleted {@link ManualReport} tagged {@link #MANUAL_REPORT_TYPE_TAG}. */
    private Set<String> manualReportMkb10Codes() {
        return manualReportRepository.findAllByReportTypeAndDeletedFalse(MANUAL_REPORT_TYPE_TAG)
                .stream()
                .flatMap(manualReport -> manualReport.getMkb10Codes().stream())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private long countTotal(
            List<Long> organizationIds, Set<String> mkb10Codes, LocalDate from, LocalDate to, long yearsAgo
    ) {
        ReportDateRange range = reportDateRangeResolver.resolve(from, to, yearsAgo);
        return form2ReportRepository
                .countTotalByMkb10Codes(organizationIds, range.fromInclusive(), range.toExclusive(), mkb10Codes)
                .total();
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }

    public record RegisteredCaseCounts(long lastYear, long currentYear) {
    }
}
