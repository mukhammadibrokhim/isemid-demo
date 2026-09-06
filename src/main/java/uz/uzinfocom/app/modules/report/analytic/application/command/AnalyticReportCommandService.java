package uz.uzinfocom.app.modules.report.analytic.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.analytic.application.command.dto.AnalyticReportCreateRequest;
import uz.uzinfocom.app.modules.report.analytic.application.command.dto.AnalyticReportUpdateRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.mapper.AnalyticReportMapper;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.AnalyticReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Full CRUD for "Analitik hisobot". {@code content}/{@code name} are stored
 * exactly as submitted — they're free-edited by the caller after the {@code
 * AnalyticReportComputeService} auto-fill, so there is no server-side
 * recomputation or diffing against the numbers here, unlike {@code
 * Form2ManualEntryCommandService}'s auto-computed counts. What <b>is</b>
 * re-validated every time is {@code regionCodes}: each one must resolve
 * inside the caller's own access scope via {@link ReportHierarchyService},
 * the same check every other report's geography drill-down applies. Update
 * and delete are additionally gated by {@link AnalyticReportOwnershipValidator}
 * — only the creating organization (or an admin) may change or remove a
 * report; read access remains the wider organization-scope rule enforced
 * separately in {@code AnalyticReportQueryService#findTable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticReportCommandService {

    private static final long DEFAULT_KOEF = 100_000L;

    private final AnalyticReportRepository analyticReportRepository;
    private final AnalyticReportMapper analyticReportMapper;
    private final AnalyticReportOwnershipValidator analyticReportOwnershipValidator;
    private final ReportHierarchyService reportHierarchyService;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public AnalyticReportResponse create(AnalyticReportCreateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();
        Set<String> regionCodes = normalize(request.regionCodes());
        validateRegionCodes(currentOrganization, regionCodes);

        AnalyticReport entity = AnalyticReport.builder()
                .organizationId(currentOrganization.getId())
                .name(request.name())
                .status(request.status())
                .fromDate(request.from())
                .toDate(request.to())
                .koef(request.koef() != null ? request.koef() : DEFAULT_KOEF)
                .content(request.content())
                .regionCodes(regionCodes)
                .icd10Codes(normalize(request.icd10Codes()))
                .build();

        AnalyticReport saved = analyticReportRepository.save(entity);
        log.debug(
                "Analytic report created. id={}, organizationId={}, status={}",
                saved.getId(), saved.getOrganizationId(), saved.getStatus()
        );

        return analyticReportMapper.toResponse(saved, currentOrganization);
    }

    @Transactional
    public AnalyticReportResponse update(Long id, AnalyticReportUpdateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();
        AnalyticReport entity = requireEntity(id);
        analyticReportOwnershipValidator.validate(entity);

        Set<String> regionCodes = normalize(request.regionCodes());
        validateRegionCodes(currentOrganization, regionCodes);

        entity.setName(request.name());
        entity.setStatus(request.status());
        entity.setFromDate(request.from());
        entity.setToDate(request.to());
        entity.setKoef(request.koef() != null ? request.koef() : DEFAULT_KOEF);
        entity.setContent(request.content());
        entity.setRegionCodes(regionCodes);
        entity.setIcd10Codes(normalize(request.icd10Codes()));

        AnalyticReport saved = analyticReportRepository.save(entity);
        log.debug("Analytic report updated. id={}, organizationId={}", saved.getId(), saved.getOrganizationId());

        Organization owningOrganization = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
        return analyticReportMapper.toResponse(saved, owningOrganization);
    }

    @Transactional
    public void delete(Long id) {
        AnalyticReport entity = requireEntity(id);
        analyticReportOwnershipValidator.validate(entity);

        analyticReportRepository.delete(entity);
        log.debug("Analytic report deleted. id={}, organizationId={}", entity.getId(), entity.getOrganizationId());
    }

    /** Every region code must be inside the caller's own access scope — same rule {@code ReportHierarchyService} enforces for report drill-downs. */
    private void validateRegionCodes(Organization currentOrganization, Set<String> regionCodes) {
        for (String regionCode : regionCodes) {
            reportHierarchyService.resolveNode(currentOrganization, regionCode, null);
        }
    }

    private Set<String> normalize(Set<String> codes) {
        if (codes == null) {
            return new HashSet<>();
        }
        return codes.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private AnalyticReport requireEntity(Long id) {
        return analyticReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report.analytic_report.not_found_by_id", id));
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
