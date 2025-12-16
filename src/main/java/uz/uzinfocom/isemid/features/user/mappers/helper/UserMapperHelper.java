package uz.uzinfocom.isemid.features.user.mappers.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

@Component
@RequiredArgsConstructor
public class UserMapperHelper {
    private final UserService userService;

    @Named("toUserMiniResponse")
    public UserMiniResponse toUserMiniResponse(Long userId) {
        if (userId == null) return null;
        User user = userService.getById(userId);
        return UserMiniResponse.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .nnuzb(user.getNnuzb())
                .birthDate(user.getBirthDate())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .fullName(user.getFullName())
                .build();
    }

}
