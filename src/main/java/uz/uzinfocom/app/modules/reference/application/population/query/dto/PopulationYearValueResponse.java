package uz.uzinfocom.app.modules.reference.application.population.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationSource;

@Schema(description = "Одно значение численности населения территории за конкретный год.")
public record PopulationYearValueResponse(

        @Schema(description = "Идентификатор записи ref_population за этот год.", example = "2")
        Long id,

        @Schema(description = "Год.", example = "2026")
        Integer year,

        @Schema(description = "Численность постоянного населения, человек.", example = "38236700")
        Long population,

        @Schema(description = "Источник данных за этот год.")
        PopulationSource source
) {
}
