package uz.uzinfocom.isemid.features.user.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.models.Permission;


public interface PermissionService {
    ApiResponse<PermissionResponse> create(PermissionRequest permissionRequest);

    ApiResponse<PermissionResponse> update(long id, PermissionRequest permissionRequest);

    ApiResponse<PermissionResponse> getBydId(long id);

    ApiResponse<?> deleteById(long id);
}
