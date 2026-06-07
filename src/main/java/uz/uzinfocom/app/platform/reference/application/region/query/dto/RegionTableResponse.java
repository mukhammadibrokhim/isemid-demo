package uz.uzinfocom.app.platform.reference.application.region.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Region reference row for paginated table responses.")
public record RegionTableResponse(
        @Schema(description = "Region internal identifier.", example = "1")
        Long id,
        @Schema(description = "Unique Region code.", example = "UZ-AN")
        String code,
        @Schema(description = "Parent Country code for the Region.", example = "UZ")
        String parentCode,
        @Schema(description = "Region SOATO identifier.", example = "1703")
        Integer soatoId,
        @Schema(description = "Region name in Uzbek Latin.", example = "Andijon viloyati")
        String nameUz,
        @Schema(description = "Region name in Uzbek Cyrillic.", example = "Андижон вилояти")
        String nameUzCyril,
        @Schema(description = "Region name in Russian.", example = "Андижанская область")
        String nameRu,
        @Schema(description = "Region name in Karakalpak.", example = "Andijan wálayatı")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted,
        @Schema(description = "Display order for Region records.", example = "10")
        Integer sortOrder
) {
}
