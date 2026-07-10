package uz.uzinfocom.app.platform.reference.application.district.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания записи района.")
public record DistrictCreateRequest(
        @Schema(
                description = "Уникальный код района.",
                example = "AN-202",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Код родительского региона для данного района.",
                example = "UZ-AN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Наименование района на узбекском языке (латиница).", example = "Oltinko‘l tumani")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование района на узбекском языке (кириллица).", example = "Олтинкўл тумани")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование района на русском языке.", example = "Алтынкульский район")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование района на каракалпакском языке.", example = "Oltinkól rayonı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
