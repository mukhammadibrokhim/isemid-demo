package uz.uzinfocom.isemid.features.user.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(target = "permissions", source = "permissions")
    RoleResponse toResponse(Role role);

    default Set<RoleResponse> toResponseSet(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(this::toResponse).collect(Collectors.toSet());
    }
}