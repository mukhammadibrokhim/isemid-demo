package uz.uzinfocom.isemid.features.user.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission permission);
}