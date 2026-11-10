package uz.uzinfocom.app.modules.iam.application.user.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import uz.uzinfocom.app.modules.iam.application.role.query.mapper.helper.RoleQueryMappingHelper;
import uz.uzinfocom.app.modules.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.modules.iam.domain.User;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = RoleQueryMappingHelper.class
)
public interface UserMeQueryMapper {

    @Mapping(
            target = "roles",
            source = "roles",
            qualifiedByName = "toRoleResponses"
    )
    UserMeResponse toMeResponse(User user);
}