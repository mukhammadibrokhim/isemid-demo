package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.domain.enums.PermissionAction;

import java.util.Set;

@Schema(description = "Разрешение, назначенное роли.")
public record RolePermissionResponse(

        @Schema(
                description = "Идентификатор разрешения.",
                example = "22"
        )
        Long id,

        @Schema(
                description = "Уникальное системное наименование разрешения.",
                example = "DISEASE_PLACES"
        )
        String subject,

        @Schema(
                description = "Локализованное описание разрешения.",
                example = "Места расположения больного"
        )
        String description,

        @Schema(
                description = "Действия, назначенные разрешению.",
                example = "[\"READ\", \"CREATE\"]"
        )
        Set<PermissionAction> actions
) {
}