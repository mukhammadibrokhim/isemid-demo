package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.user.query.mapper.UserQueryMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserTableProjection;
import uz.uzinfocom.app.platform.iam.application.user.query.specification.UserSpecification;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserTableResponse;
import uz.uzinfocom.app.platform.web.pagination.PageableUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepo;
    private final UserQueryMapper userQueryMapper;
    private final UserOrganizationRoleQueryService userOrganizationRoleQueryService;
    private final MessageResolver messageResolver;

    @Transactional(readOnly = true)
    public Page<UserTableResponse> findTable(UserFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                UserSortFields.ALLOWED
        );

        Page<UserTableProjection> page = userRepo.findBy(
                UserSpecification.byFilter(request),
                query -> query
                        .as(UserTableProjection.class)
                        .page(pageable)
        );

        return page.map(userQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getRequiredByUuid(UUID uuid) {
        User user = userRepo.findByUuid(uuid).orElseThrow(() -> new NotFoundException(messageResolver.resolve("error.user.not_found")));
        return withOrganizationRoles(userQueryMapper.toDetailedResponse(user), user);
    }

    @Transactional(readOnly = true)
    public UserDetailedResponse getRequiredById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageResolver.resolve("error.user.not_found")
                ));

        return withOrganizationRoles(userQueryMapper.toDetailedResponse(user), user);
    }

    private UserDetailedResponse withOrganizationRoles(UserDetailedResponse response, User user) {
        return new UserDetailedResponse(
                response.id(),
                response.uuid(),
                response.active(),
                response.firstName(),
                response.lastName(),
                response.middleName(),
                response.nnuzb(),
                response.birthDate(),
                response.phoneNumber(),
                response.stateCode(),
                response.cityCode(),
                response.line(),
                response.genderCode(),
                response.organizations(),
                response.roles(),
                userOrganizationRoleQueryService.findOrganizationsWithRoles(user, null)
        );
    }
}
