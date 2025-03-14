package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.payload.requests.ReqPermission;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface PermissionService {
    ApiResponse<Permission> create(ReqPermission reqPermission);

    ApiResponse<Permission> update(long id, ReqPermission reqPermission);

    ApiResponse<Permission> getBydId(long id);

    ApiResponse<?> deleteById(long id);
}
