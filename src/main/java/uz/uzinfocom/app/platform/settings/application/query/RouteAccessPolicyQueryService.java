package uz.uzinfocom.app.platform.settings.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public RouteAccessPolicyResponse getById(Long id) {
        RouteAccessPolicy policy = routeAccessPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("route-policy.not-found", id));

        return toResponse(policy);
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
}
