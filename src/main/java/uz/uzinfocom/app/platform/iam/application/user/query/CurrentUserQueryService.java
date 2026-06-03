package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.RolePermissionResponseMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserQueryService {

    private final UserRepository userRepository;
    private final RolePermissionResponseMapper rolePermissionResponseMapper;

    @Transactional(readOnly = true)
    public UserMeResponse getCurrentUser(Long userId) {
        User user = userRepository.findForAuthorizationById(userId)
                .orElseThrow(() -> new NotFoundException("user.not_found"));

        return new UserMeResponse(
                user.getId(),
                user.getUuid(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getActive(),
                rolePermissionResponseMapper.toRoleResponses(user.getRoles())
        );
    }
}
