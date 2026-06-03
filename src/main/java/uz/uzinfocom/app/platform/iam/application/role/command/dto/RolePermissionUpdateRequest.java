package uz.uzinfocom.app.platform.iam.application.role.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Запрос на изменение прав доступа роли.")
public record RolePermissionUpdateRequest(

        @Schema(description = "Список прав доступа и действий, назначаемых роли.")
        @Valid
        @NotEmpty(message = "{permission.ids.required}")
        List<RolePermissionItemRequest> permissions
) {
}
