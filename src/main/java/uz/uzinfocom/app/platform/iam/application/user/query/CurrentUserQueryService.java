package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.mapper.UserMeMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserMeProjection;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserQueryService {

    private final UserRepository userRepository;
    private final UserMeMapper userMeMapper;
    private final UserOrganizationRoleQueryService userOrganizationRoleQueryService;

    @Transactional(readOnly = true)
    public UserMeResponse getCurrentUser(Long userId, UUID selectedOrganizationUuid) {
        UserMeProjection user = userRepository.findMeProjectionById(userId)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));
        return userMeMapper.meResponse(user);
    }
}
