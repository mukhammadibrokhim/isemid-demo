package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.payload.requests.ReqPermission;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface PermissionService {
    ApiResponse<Permission> createPermission(ReqPermission reqPermission);

    ApiResponse<Permission> getBydId(long id);

    ApiResponse<Permission> updatePermissionById(long id, ReqPermission reqPermission);

    ApiResponse<?> deleteById(long id);
}
