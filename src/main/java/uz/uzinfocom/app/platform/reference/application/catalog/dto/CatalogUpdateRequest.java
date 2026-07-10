package uz.uzinfocom.app.platform.reference.application.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для обновления элемента каталога.")
public record CatalogUpdateRequest(
        @Schema(
                description = "Тип каталога.",
                example = "GENDER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{reference.catalog.type.required}")
        String type,

        @Schema(
                description = "Уникальный код элемента внутри выбранного типа каталога.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Необязательный код родительского элемента внутри того же типа каталога.")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Наименование элемента каталога на узбекском языке (латиница).")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование элемента каталога на узбекском языке (кириллица).")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование элемента каталога на русском языке.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование элемента каталога на каракалпакском языке.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
