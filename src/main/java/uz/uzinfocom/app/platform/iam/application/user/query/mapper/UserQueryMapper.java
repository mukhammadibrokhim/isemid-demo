package uz.uzinfocom.app.platform.iam.application.user.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.RoleQueryMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserTableProjection;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserTableResponse;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleQueryMapper.class
        }
)
public interface UserQueryMapper {

    UserDetailedResponse toDetailedResponse(User user);

    UserTableResponse toTableResponse(UserTableProjection projection);
}