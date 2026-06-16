package uz.uzinfocom.app.platform.iam.application.user.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMiniResponse;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserMapperHelper {

    private final UserRepository userRepository;

    @Named("toUserMiniResponse")
    public UserMiniResponse toUserMiniResponse(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).map(this::toUserMiniResponse).orElse(null);
    }

    @Named("toUserMiniResponse")
    public UserMiniResponse toUserMiniResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserMiniResponse(
                user.getId(),
                user.getUuid(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getFullName()
        );
    }
}
