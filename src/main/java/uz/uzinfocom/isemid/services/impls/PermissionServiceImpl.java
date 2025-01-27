package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.payload.requests.ReqPermission;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.services.PermissionService;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    final PermissionRepository permissionRepository;

    @Override
    public ApiResponse<Permission> createPermission(ReqPermission reqPermission) {
        Permission permission = Permission.builder()
                .name(reqPermission.getName())
                .descriptionUz(reqPermission.getDescriptionUz())
                .descriptionUzCyril(reqPermission.getDescriptionUzCyril())
                .descriptionRu(reqPermission.getDescriptionRu())
                .descriptionKaa(reqPermission.getDescriptionKaa())
                .build();
        return new ApiResponse<>(true, "Success", permissionRepository.save(permission));
    }
}
