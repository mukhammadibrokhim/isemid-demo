package uz.uzinfocom.app.modules.reference.application.population.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;

@Schema(description = "Данные для создания записи справочника численности населения (ручной ввод). "
        + "Наименование территории не хранится — оно разрешается по soatoId / коду области / района.")
public record PopulationCreateRequest(

        @Schema(description = "Уровень административной иерархии.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.geo_type.required}")
        PopulationGeoType geoType,

        @Schema(description = "Код территории по МХОБТ/СОАТО.", example = "1703", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.soato_id.required}")
        @Min(value = 1, message = "{reference.population.soato_id.positive}")
        Integer soatoId,

        @Schema(description = "Код области (ref_region.code), если применимо.", example = "UZ-AN")
        @Size(max = 50, message = "{reference.code.max_length}")
        String regionCode,

        @Schema(description = "Код района (ref_district.code), если применимо.", example = "AN-203")
        @Size(max = 50, message = "{reference.code.max_length}")
        String districtCode,

        @Schema(description = "Год, к которому относится численность.", example = "2025", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.year.required}")
        @Min(value = 1900, message = "{reference.population.year.range}")
        @Max(value = 2100, message = "{reference.population.year.range}")
        Integer year,

        @Schema(description = "Численность постоянного населения, человек (абсолютное значение).", example = "37543200",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{reference.population.population.required}")
        @PositiveOrZero(message = "{reference.population.population.positive}")
        Long population
) {
}
