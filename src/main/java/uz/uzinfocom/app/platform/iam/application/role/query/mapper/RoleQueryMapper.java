package uz.uzinfocom.app.platform.iam.application.role.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper.RoleQueryMappingHelper;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.domain.Role;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleQueryMappingHelper.class,
        }
)
public interface RoleQueryMapper {

    RoleTableResponse toTableResponse(RoleTableProjection projection);

    RoleDetailResponse toDetailResponse(Role role);

    @Mapping(target = "description", source = "role", qualifiedByName = "roleDescription")
    RoleShortResponse toShortResponse(Role role);

    @Mapping(target = "description", source = "projection", qualifiedByName = "roleProjectionDescription")
    RoleShortResponse toShortResponse(RoleTableProjection projection);
}
