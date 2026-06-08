package uz.uzinfocom.app.platform.reference.application.district.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed District reference response.")
public record DistrictResponse(
        @Schema(description = "District internal identifier.", example = "1")
        Long id,
        @Schema(description = "Unique District code.", example = "AN-202")
        String code,
        @Schema(description = "Parent Region code for the District.", example = "UZ-AN")
        String parentCode,
        @Schema(description = "District name in Uzbek Latin.", example = "Oltinko‘l tumani")
        String nameUz,
        @Schema(description = "District name in Uzbek Cyrillic.", example = "Олтинкўл тумани")
        String nameUzCyril,
        @Schema(description = "District name in Russian.", example = "Алтынкульский район")
        String nameRu,
        @Schema(description = "District name in Karakalpak.", example = "Oltinkól rayonı")
        String nameKaa,
        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}
