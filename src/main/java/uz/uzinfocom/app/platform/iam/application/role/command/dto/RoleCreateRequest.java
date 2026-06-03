package uz.uzinfocom.app.platform.iam.application.role.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание роли.")
public record RoleCreateRequest(

        @Schema(description = "Наименование роли.", example = "ROLE_ADMIN")
        @NotBlank(message = "{role.name.required}")
        @Size(max = 150, message = "{role.name.max_length}")
        String name,

        @Schema(description = "Описание роли на узбекском языке.", example = "Administrator roli")
        @Size(max = 1000, message = "{role.description.max_length}")
        String descriptionUz,

        @Schema(description = "Описание роли на русском языке.", example = "Роль администратора")
        @Size(max = 1000, message = "{role.description.max_length}")
        String descriptionRu,

        @Schema(description = "Описание роли на узбекском языке кириллицей.")
        @Size(max = 1000, message = "{role.description.max_length}")
        String descriptionUzCyril,

        @Schema(description = "Описание роли на каракалпакском языке.")
        @Size(max = 1000, message = "{role.description.max_length}")
        String descriptionKaa,

        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active
) {
}
