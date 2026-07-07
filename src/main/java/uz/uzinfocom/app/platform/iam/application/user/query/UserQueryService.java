package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.application.user.query.mapper.UserQueryMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserTableProjection;
import uz.uzinfocom.app.platform.iam.application.user.query.specification.UserSpecification;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserTableResponse;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepo;
    private final UserQueryMapper userQueryMapper;
    private final AuditResolver auditResolver;
    private final OrganizationNameResolver organizationNameResolver;

    @Transactional(readOnly = true)
    public Page<UserTableResponse> findTable(UserFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                UserSortFields.ALLOWED
        );

        Page<UserTableProjection> page = Objects.requireNonNull(userRepo.findBy(
                        UserSpecification.byFilter(request),
                        query -> query
                                .as(UserTableProjection.class)
                                .page(pageable)
                ), "User table page return null!"
        );

        return page.map(userQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getRequiredByUuid(UUID uuid) {
        User user = userRepo.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("user.not_found"));

        return userQueryMapper.toDetailedResponse(user, auditResolver.resolve(user));
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getRequiredById(Long id) {
        User user = userRepo.findWithOrganizationsById(id)
                .orElseThrow(() -> new NotFoundException("user.not_found"));

        return userQueryMapper.toDetailedResponse(user, auditResolver.resolve(user));
    }

    @Transactional(readOnly = true)
    public List<OrganizationShortResponse> findOrganizations(Long userId) {
        User user = userRepo.findWithOrganizationsById(userId)
                .orElseThrow(() -> new NotFoundException("user.not_found"));

        if (user.getOrganizations() == null) {
            return List.of();
        }

        return user.getOrganizations().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        Organization::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .map(organization -> new OrganizationShortResponse(
                        organization.getId(),
                        organization.getUuid(),
                        organizationNameResolver.resolve(organization)
                ))
                .toList();
    }
}
