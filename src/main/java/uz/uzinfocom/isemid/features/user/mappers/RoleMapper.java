package uz.uzinfocom.isemid.features.user.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;
import uz.uzinfocom.isemid.features.user.models.Role;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "permissions", source = "permissions")
    RoleResponse toResponse(Role role);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRole(RoleRequest request, @MappingTarget Role role);

    default Set<RoleResponse> toResponseSet(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream().map(this::toResponse).collect(Collectors.toSet());
    }
}