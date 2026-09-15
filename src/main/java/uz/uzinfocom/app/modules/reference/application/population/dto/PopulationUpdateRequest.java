package uz.uzinfocom.app.modules.reference.application.population.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;

@Schema(description = "Данные для обновления записи справочника численности населения. "
        + "Территория (soatoId) и год не изменяются — это идентичность записи.")
public record PopulationUpdateRequest(

        @Schema(description = "Уровень административной иерархии.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.geo_type.required}")
        PopulationGeoType geoType,

        @Schema(description = "Код области (ref_region.code), если применимо.", example = "UZ-AN")
        @Size(max = 50, message = "{reference.code.max_length}")
        String regionCode,

        @Schema(description = "Код района (ref_district.code), если применимо.", example = "AN-203")
        @Size(max = 50, message = "{reference.code.max_length}")
        String districtCode,

        @Schema(description = "Численность постоянного населения, человек (абсолютное значение).", example = "37543200",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.population.required}")
        @PositiveOrZero(message = "{reference.population.population.positive}")
        Long population
) {
}
