package uz.uzinfocom.app.platform.reference.application.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a Region reference record.")
public record RegionCreateRequest(
        @Schema(
                description = "Unique Region code.",
                example = "UZ-AN",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Parent Country code for the Region.",
                example = "UZ",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Region name in Uzbek Latin.", example = "Andijon viloyati")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Region name in Uzbek Cyrillic.", example = "Андижон вилояти")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Region name in Russian.", example = "Андижанская область")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Region name in Karakalpak.", example = "Andijan wálayatı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @Schema(description = "Display order for Region records.", example = "10")
        @PositiveOrZero(message = "{validation.must_be_positive}")
        Integer sortOrder
) {
}
