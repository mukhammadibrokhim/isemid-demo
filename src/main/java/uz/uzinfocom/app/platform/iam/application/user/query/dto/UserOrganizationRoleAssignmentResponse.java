package uz.uzinfocom.app.platform.iam.application.user.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

@Schema(description = "Результат назначения ролей пользователю в организации.")
public record UserOrganizationRoleAssignmentResponse(
        @Schema(description = "Уникальный идентификатор пользователя.", example = "1")
        Long userId,
        @Schema(description = "Уникальный UUID пользователя.")
        UUID userUuid,
        @Schema(description = "Логин пользователя.")
        String username,
        @Schema(description = "ННУЗБ пользователя.")
        String nnuzb,
        @Schema(description = "Организация, в рамках которой назначены роли.")
        OrganizationShortResponse organization,
        @Schema(description = "Роли пользователя в организации.")
        List<RoleResponse> roles
) {
}
