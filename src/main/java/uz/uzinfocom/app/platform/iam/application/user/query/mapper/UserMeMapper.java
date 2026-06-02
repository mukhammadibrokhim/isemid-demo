package uz.uzinfocom.app.platform.iam.application.user.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.RoleQueryMapper;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserMeProjection;

@Mapper(componentModel = "spring", uses = RoleQueryMapper.class)
public interface UserMeMapper {

    UserMeResponse meResponse(UserMeProjection entity);
}
