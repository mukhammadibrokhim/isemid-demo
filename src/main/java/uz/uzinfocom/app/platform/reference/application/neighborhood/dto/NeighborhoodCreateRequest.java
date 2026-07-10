package uz.uzinfocom.app.platform.reference.application.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания записи махалли.")
public record NeighborhoodCreateRequest(
        @Schema(
                description = "Уникальный код махалли.",
                example = "AN-202001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Код родительского района для данной махалли.",
                example = "AN-202",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Наименование махалли на узбекском языке (латиница).", example = "Dalvarzin")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование махалли на узбекском языке (кириллица).", example = "Далварзин")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование махалли на русском языке.", example = "Далварзин")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование махалли на каракалпакском языке.", example = "Dalvarzin")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
