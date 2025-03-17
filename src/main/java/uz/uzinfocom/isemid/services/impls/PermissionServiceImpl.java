package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.payload.requests.ReqPermission;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.PermissionRepository;
import uz.uzinfocom.isemid.services.PermissionService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    final PermissionRepository permissionRepository;

    @Override
    public ApiResponse<Permission> create(ReqPermission reqPermission) {
        Permission permission = Permission.builder()
                .subject(reqPermission.getSubject())
                .actions(reqPermission.getActions())
                .descriptionUz(reqPermission.getDescriptionUz())
                .descriptionUzCyril(reqPermission.getDescriptionUzCyril())
                .descriptionRu(reqPermission.getDescriptionRu())
                .descriptionKaa(reqPermission.getDescriptionKaa())
                .build();
        return new ApiResponse<>(true, "Success", permissionRepository.save(permission));
    }

    @Override
    public ApiResponse<Permission> getBydId(long id) {
        return new ApiResponse<>(true, "Success",
                permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found.")));
    }

    @Override
    public ApiResponse<Permission> update(long id, ReqPermission reqPermission) {
        Permission exsistingPermission = permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Permission not found."));
        Optional.ofNullable(reqPermission.getSubject()).ifPresent(exsistingPermission::setSubject);
        Optional.ofNullable(reqPermission.getActions()).ifPresent(exsistingPermission::setActions);
        Optional.ofNullable(reqPermission.getActive()).ifPresent(exsistingPermission::setActive);
        Optional.ofNullable(reqPermission.getDescriptionUz()).ifPresent(exsistingPermission::setDescriptionUz);
        Optional.ofNullable(reqPermission.getDescriptionRu()).ifPresent(exsistingPermission::setDescriptionRu);
        Optional.ofNullable(reqPermission.getDescriptionUzCyril()).ifPresent(exsistingPermission::setDescriptionUzCyril);
        Optional.ofNullable(reqPermission.getDescriptionKaa()).ifPresent(exsistingPermission::setDescriptionKaa);
        return new ApiResponse<>(true, "Successfully updated.", permissionRepository.save(exsistingPermission));
    }

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            permissionRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Internal Server Error", e);
        }
        return new ApiResponse<>(true, "Successfully deleted.", null);
    }
}
