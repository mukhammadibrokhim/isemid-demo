package uz.uzinfocom.app.modules.iam.application.user.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.iam.application.organization.query.mapper.OrganizationMapperHelper;
import uz.uzinfocom.app.modules.iam.application.role.query.mapper.RoleQueryMapper;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;
import uz.uzinfocom.app.modules.iam.application.user.query.projection.UserTableProjection;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.modules.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.modules.iam.web.user.dto.response.UserTableResponse;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleQueryMapper.class,
                OrganizationMapperHelper.class
        }
)
public interface UserQueryMapper {

    @Mapping(target = "audit", source = "audit")
    UserDetailedResponse toDetailedResponse(User user, AuditResponse audit);

    UserTableResponse toTableResponse(UserTableProjection projection);
}