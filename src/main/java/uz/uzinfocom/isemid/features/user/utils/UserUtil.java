package uz.uzinfocom.isemid.features.user.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@UtilityClass
public class UserUtil {
    public static void checkSuperAdminConstraint(UserRepository userRepository, User user, MessageUtil messageUtil) {
        boolean hasSuperAdminRole = user.getRoles().stream()
                .anyMatch(role -> "isemid_super_admin".equals(role.getName()));

        if (hasSuperAdminRole) {
            boolean exists = userRepository.existsSuperAdminForOtherUser("isemid_super_admin", user.getId());
            if (exists) {
                throw new ApiException(BAD_REQUEST, messageUtil.get("error.unique_super_admin"));
            }
        }
    }
}
