package uz.uzinfocom.app.platform.iam.application.permission.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление права доступа.")
public record PermissionUpdateRequest(

        @Schema(description = "Субъект права доступа.", example = "users")
        @NotBlank(message = "subject is required")
        @Size(max = 150, message = "subject max length is 150")
        String subject,

        @Schema(description = "Описание права доступа на узбекском языке.")
        @Size(max = 1000, message = "descriptionUz max length is 1000")
        String descriptionUz,

        @Schema(description = "Описание права доступа на русском языке.", example = "Управление пользователями")
        @Size(max = 1000, message = "descriptionRu max length is 1000")
        String descriptionRu,

        @Schema(description = "Описание права доступа на узбекском языке кириллицей.")
        @Size(max = 1000, message = "descriptionUzCyril max length is 1000")
        String descriptionUzCyril,

        @Schema(description = "Описание права доступа на каракалпакском языке.")
        @Size(max = 1000, message = "descriptionKaa max length is 1000")
        String descriptionKaa,

        @Schema(description = "Признак активности записи.", example = "true")
        @NotNull(message = "active is required")
        Boolean active
) {
}
