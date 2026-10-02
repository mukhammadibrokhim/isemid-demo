package uz.uzinfocom.app.platform.settings.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyDetailResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyFilterRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyResponse;
import uz.uzinfocom.app.platform.settings.application.query.specification.RouteAccessPolicySpecification;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;
import uz.uzinfocom.app.platform.settings.repository.RouteAccessPolicyRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class RouteAccessPolicyQueryService {

    private final RouteAccessPolicyRepository routeAccessPolicyRepository;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<RouteAccessPolicyResponse> findAll(RouteAccessPolicyFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request, "displayOrder", Sort.Direction.ASC, RouteAccessPolicySortFields.ALLOWED
        );

        return routeAccessPolicyRepository
                .findAll(RouteAccessPolicySpecification.byFilter(request), pageable)
                .map(RouteAccessPolicyQueryService::toResponse);
    }

    @Transactional(readOnly = true)
    public RouteAccessPolicyDetailResponse getById(Long id) {
        RouteAccessPolicy policy = routeAccessPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("route-policy.not-found", id));

        return toDetailResponse(policy, auditResolver.resolve(policy));
    }

    private static RouteAccessPolicyResponse toResponse(RouteAccessPolicy entity) {
        return new RouteAccessPolicyResponse(
                entity.getId(),
                entity.getPattern(),
                entity.getDisplayOrder(),
                entity.getOpen(),
                entity.getOrganizationHeaderRequired(),
                entity.getRoleValidationRequired(),
                entity.getEnabled(),
                entity.getDescription()
        );
    }

    private static RouteAccessPolicyDetailResponse toDetailResponse(RouteAccessPolicy entity, AuditResponse audit) {
        return new RouteAccessPolicyDetailResponse(
                entity.getId(),
                entity.getPattern(),
                entity.getDisplayOrder(),
                entity.getOpen(),
                entity.getOrganizationHeaderRequired(),
                entity.getRoleValidationRequired(),
                entity.getEnabled(),
                entity.getDescription(),
                audit
        );
    }
}
