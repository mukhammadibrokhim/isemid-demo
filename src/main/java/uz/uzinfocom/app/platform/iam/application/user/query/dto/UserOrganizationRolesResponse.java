package uz.uzinfocom.app.platform.iam.application.user.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleResponse;

import java.util.List;

@Schema(description = "Организация пользователя с ролями и правами доступа в этой организации.")
public record UserOrganizationRolesResponse(
        @Schema(description = "Организация пользователя.")
        OrganizationShortResponse organization,
        @Schema(description = "Признак выбранной текущей организации пользователя.", example = "true")
        Boolean selected,
        @Schema(description = "Роли пользователя в этой организации.")
        List<RoleResponse> roles,
        @Schema(description = "Итоговые права доступа пользователя в этой организации.")
        List<PermissionResponse> permissions
) {
}
