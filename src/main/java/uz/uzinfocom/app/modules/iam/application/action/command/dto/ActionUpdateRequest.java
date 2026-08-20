package uz.uzinfocom.app.modules.iam.application.action.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление действия.")
public record ActionUpdateRequest(

        @Schema(description = "Код действия.", example = "CARD_ATTACH")
        @NotBlank(message = "{action.code.required}")
        @Size(max = 100, message = "{action.code.max_length}")
        String code,

        @Schema(description = "Описание действия на узбекском языке.")
        @Size(max = 1000, message = "{action.description.max_length}")
        String descriptionUz,

        @Schema(description = "Описание действия на русском языке.")
        @Size(max = 1000, message = "{action.description.max_length}")
        String descriptionRu,

        @Schema(description = "Описание действия на узбекском языке кириллицей.")
        @Size(max = 1000, message = "{action.description.max_length}")
        String descriptionUzCyril,

        @Schema(description = "Описание действия на каракалпакском языке.")
        @Size(max = 1000, message = "{action.description.max_length}")
        String descriptionKaa,

        @Schema(description = "Признак активности записи.", example = "true")
        @NotNull(message = "{action.active.required}")
        Boolean active
) {
}
