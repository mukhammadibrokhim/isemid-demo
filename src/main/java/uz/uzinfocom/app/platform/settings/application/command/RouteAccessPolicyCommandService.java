package uz.uzinfocom.app.platform.settings.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.settings.application.RouteAccessPolicyResolver;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyResponse;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;
import uz.uzinfocom.app.platform.settings.repository.RouteAccessPolicyRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAccessPolicyCommandService {

    private final RouteAccessPolicyRepository routeAccessPolicyRepository;
    private final RouteAccessPolicyResolver routeAccessPolicyResolver;

    @Transactional
    public RouteAccessPolicyResponse create(RouteAccessPolicyCreateRequest request) {
        String pattern = request.pattern().trim();

        if (routeAccessPolicyRepository.existsByPatternIgnoreCase(pattern)) {
            throw new ConflictException("route-policy.pattern.already-exists", pattern);
        }

        RouteAccessPolicy policy = RouteAccessPolicy.builder()
                .pattern(pattern)
                .displayOrder(request.displayOrder())
                .open(Boolean.TRUE.equals(request.open()))
                .organizationHeaderRequired(request.organizationHeaderRequired() == null || request.organizationHeaderRequired())
                .roleValidationRequired(request.roleValidationRequired() == null || request.roleValidationRequired())
                .enabled(request.enabled() == null || request.enabled())
                .description(request.description())
                .build();

        RouteAccessPolicy saved = routeAccessPolicyRepository.save(policy);
        routeAccessPolicyResolver.evictAll();
        log.info("Route access policy created. id={}, pattern={}", saved.getId(), saved.getPattern());

        return toResponse(saved);
    }

    @Transactional
    public RouteAccessPolicyResponse update(Long id, RouteAccessPolicyUpdateRequest request) {
        RouteAccessPolicy policy = routeAccessPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("route-policy.not-found", id));

        String newPattern = request.pattern().trim();
        if (!newPattern.equalsIgnoreCase(policy.getPattern())
                && routeAccessPolicyRepository.existsByPatternIgnoreCase(newPattern)) {
            throw new ConflictException("route-policy.pattern.already-exists", newPattern);
        }

        policy.setPattern(newPattern);
        policy.setDisplayOrder(request.displayOrder());
        policy.setOpen(Boolean.TRUE.equals(request.open()));
        policy.setOrganizationHeaderRequired(request.organizationHeaderRequired() == null || request.organizationHeaderRequired());
        policy.setRoleValidationRequired(request.roleValidationRequired() == null || request.roleValidationRequired());
        if (request.enabled() != null) {
            policy.setEnabled(request.enabled());
        }
        policy.setDescription(request.description());

        RouteAccessPolicy saved = routeAccessPolicyRepository.save(policy);
        routeAccessPolicyResolver.evictAll();
        log.info("Route access policy updated. id={}, pattern={}", saved.getId(), saved.getPattern());

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        RouteAccessPolicy policy = routeAccessPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("route-policy.not-found", id));

        routeAccessPolicyRepository.delete(policy);
        routeAccessPolicyResolver.evictAll();

        log.info("Route access policy deleted. id={}, pattern={}", policy.getId(), policy.getPattern());
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
