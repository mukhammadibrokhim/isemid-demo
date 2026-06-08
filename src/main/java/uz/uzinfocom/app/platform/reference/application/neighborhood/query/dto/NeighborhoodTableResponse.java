package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Neighborhood reference row for paginated table responses.")
public record NeighborhoodTableResponse(
        @Schema(description = "Neighborhood internal identifier.", example = "1")
        Long id,
        @Schema(description = "Unique Neighborhood code.", example = "AN-202001")
        String code,
        @Schema(description = "Parent District code for the Neighborhood.", example = "AN-202")
        String parentCode,
        @Schema(description = "Neighborhood SOATO identifier.", example = "1703202001")
        Integer soatoId,
        @Schema(description = "Parent District SOATO identifier.", example = "1703202")
        Integer parentSoatoId,
        @Schema(description = "Neighborhood name in Uzbek Latin.", example = "Dalvarzin")
        String nameUz,
        @Schema(description = "Neighborhood name in Uzbek Cyrillic.", example = "Далварзин")
        String nameUzCyril,
        @Schema(description = "Neighborhood name in Russian.", example = "Далварзин")
        String nameRu,
        @Schema(description = "Neighborhood name in Karakalpak.", example = "Dalvarzin")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}
