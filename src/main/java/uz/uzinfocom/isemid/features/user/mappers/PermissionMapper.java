package uz.uzinfocom.isemid.features.user.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.models.Permission;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {
    Permission toEntity(PermissionRequest request);
    @Mapping(target = "id", source = "id")
    PermissionResponse toResponse(Permission permission);

    @Mapping(target = "id", ignore = true)
    void updatePermission(PermissionRequest request, @MappingTarget Permission entity);
}