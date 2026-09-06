package uz.uzinfocom.app.modules.report.analytic.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportFilterRequest;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportTableResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.mapper.AnalyticReportMapper;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.AnalyticReportRepository;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.specification.AnalyticReportSpecification;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organization-scoped table listing and detail lookup for "Analitik
 * hisobot" — mirrors {@code Form2ManualEntryQueryService.findTable}.
 */
@Service
@RequiredArgsConstructor
public class AnalyticReportQueryService {

    private final AnalyticReportRepository analyticReportRepository;
    private final AnalyticReportSpecification analyticReportSpecification;
    private final AnalyticReportMapper analyticReportMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    @Transactional(readOnly = true)
    public Page<AnalyticReportTableResponse> findTable(AnalyticReportFilterRequest request) {
        AnalyticReportFilterRequest filter = request == null
                ? new AnalyticReportFilterRequest(null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, AnalyticReportSortFields.ALLOWED_SORT_FIELDS);
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());

        Page<AnalyticReport> page = analyticReportRepository.findAll(
                analyticReportSpecification.byFilter(filter, scope), pageable
        );

        Map<Long, Organization> organizationsById = organizationRepository
                .findAllById(page.getContent().stream().map(AnalyticReport::getOrganizationId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return page.map(entity -> analyticReportMapper.toTableResponse(
                entity, organizationsById.get(entity.getOrganizationId())
        ));
    }

    @Transactional(readOnly = true)
    public AnalyticReportResponse getById(Long id) {
        AnalyticReport entity = requireEntity(id);
        Organization organization = organizationRepository.findById(entity.getOrganizationId()).orElse(null);
        return analyticReportMapper.toResponse(entity, organization);
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
