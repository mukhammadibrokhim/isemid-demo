package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.mapper.UserMeQueryMapper;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class CurrentUserQueryService {

    private final UserRepository userRepository;
    private final UserMeQueryMapper userMeQueryMapper;

    @Transactional(readOnly = true)
    public UserMeResponse getCurrentUser(Long userId) {
        User user = userRepository.findForAuthorizationById(userId)
                .orElseThrow(() ->
                        new NotFoundException("user.not_found")
                );

        return userMeQueryMapper.toMeResponse(user);
    }
}