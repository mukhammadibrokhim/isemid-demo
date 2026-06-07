package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed Neighborhood reference response.")
public record NeighborhoodResponse(
        @Schema(description = "Neighborhood internal identifier.", example = "1")
        Long id,
        @Schema(description = "Unique Neighborhood code.", example = "AN-202001")
        String code,
        @Schema(description = "Parent District code for the Neighborhood.", example = "AN-202")
        String parentCode,
        @Schema(description = "Neighborhood name in Uzbek Latin.", example = "Dalvarzin")
        String nameUz,
        @Schema(description = "Neighborhood name in Uzbek Cyrillic.", example = "Далварзин")
        String nameUzCyril,
        @Schema(description = "Neighborhood name in Russian.", example = "Далварзин")
        String nameRu,
        @Schema(description = "Neighborhood name in Karakalpak.", example = "Dalvarzin")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted,
        @Schema(description = "Display order for Neighborhood records.", example = "10")
        Integer sortOrder
) {
}
