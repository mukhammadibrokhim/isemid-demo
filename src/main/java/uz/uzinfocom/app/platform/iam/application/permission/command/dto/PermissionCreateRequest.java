package uz.uzinfocom.app.platform.iam.application.permission.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание права доступа.")
public record PermissionCreateRequest(

        @Schema(description = "Субъект права доступа.", example = "users")
        @NotBlank(message = "{permission.subject.required}")
        @Size(max = 150, message = "{permission.subject.max_length}")
        String subject,

        @Schema(description = "Описание права доступа на узбекском языке.")
        @Size(max = 1000, message = "{permission.description.max_length}")
        String descriptionUz,

        @Schema(description = "Описание права доступа на русском языке.", example = "Управление пользователями")
        @Size(max = 1000, message = "{permission.description.max_length}")
        String descriptionRu,

        @Schema(description = "Описание права доступа на узбекском языке кириллицей.")
        @Size(max = 1000, message = "{permission.description.max_length}")
        String descriptionUzCyril,

        @Schema(description = "Описание права доступа на каракалпакском языке.")
        @Size(max = 1000, message = "{permission.description.max_length}")
        String descriptionKaa,

        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active
) {
}
