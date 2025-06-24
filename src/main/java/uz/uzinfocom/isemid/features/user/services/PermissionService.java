package uz.uzinfocom.isemid.features.user.services;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;

public interface PermissionService {
    PagedResponse<?> getAllPermissionsPage(
            int page, int size, String subject,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa,
            Boolean active,
            String requestUrl
    );

    ApiResponse<PermissionResponse> create(PermissionRequest permissionRequest);

    ApiResponse<PermissionResponse> update(long id, PermissionRequest permissionRequest);

    ApiResponse<PermissionResponse> getBydId(long id);

    ApiResponse<?> deleteById(long id);
}
