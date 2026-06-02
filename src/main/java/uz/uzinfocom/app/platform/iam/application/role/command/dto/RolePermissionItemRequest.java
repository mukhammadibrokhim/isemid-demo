package uz.uzinfocom.app.platform.iam.application.role.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import uz.uzinfocom.app.platform.iam.domain.enums.PermissionAction;

import java.util.Set;

@Schema(description = "Право доступа и набор действий внутри роли.")
public record RolePermissionItemRequest(

        @Schema(description = "Идентификатор права доступа.", example = "1")
        @NotNull(message = "permissionId is required")
        @Positive(message = "permissionId must be positive")
        Long permissionId,

        @Schema(description = "Список разрешенных действий для права доступа.", example = "[\"VIEW\", \"CREATE\"]")
        @NotEmpty(message = "actions must not be empty")
        Set<PermissionAction> actions
) {
}
