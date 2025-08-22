package uz.uzinfocom.isemid.features.user.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.models.Permission;

public interface PermissionService {
    Page<Permission> filter(
            int page, int size, String subject,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa,
            Boolean active
    );

    Permission create(PermissionRequest permissionRequest);

    Permission update(long id, PermissionRequest permissionRequest);

    Permission getBydId(long id);

    void deleteById(long id);
}
