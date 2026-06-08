package uz.uzinfocom.app.platform.reference.application.neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a Neighborhood reference record.")
public record NeighborhoodCreateRequest(
        @Schema(
                description = "Unique Neighborhood code.",
                example = "AN-202001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(
                description = "Parent District code for the Neighborhood.",
                example = "AN-202",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.parent_code.required}")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Neighborhood name in Uzbek Latin.", example = "Dalvarzin")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Neighborhood name in Uzbek Cyrillic.", example = "Далварзин")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Neighborhood name in Russian.", example = "Далварзин")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Neighborhood name in Karakalpak.", example = "Dalvarzin")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa
) {
}
