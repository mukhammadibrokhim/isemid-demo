package uz.uzinfocom.app.platform.reference.application.district.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a District reference record.")
public record DistrictCreateRequest(
        @Schema(
                description = "Unique District code.",
                example = "AN-202",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Parent Region code for the District.",
                example = "UZ-AN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "District name in Uzbek Latin.", example = "Oltinko‘l tumani")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "District name in Uzbek Cyrillic.", example = "Олтинкўл тумани")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "District name in Russian.", example = "Алтынкульский район")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "District name in Karakalpak.", example = "Oltinkól rayonı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
