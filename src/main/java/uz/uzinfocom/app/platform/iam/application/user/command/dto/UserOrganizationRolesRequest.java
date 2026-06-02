package uz.uzinfocom.app.platform.iam.application.user.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Запрос на назначение ролей пользователю в организации.")
public record UserOrganizationRolesRequest(

        @Schema(description = "Список идентификаторов ролей.", example = "[1, 2]")
        @NotEmpty(message = "roleIds must not be empty")
        List<@Positive(message = "roleId must be positive") Long> roleIds
) {
}
