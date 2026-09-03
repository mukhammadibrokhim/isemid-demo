package uz.uzinfocom.app.modules.reference.application.population.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationSource;

@Schema(description = "Узел иерархии численности населения: республика / область / район за выбранный год.")
public record PopulationNodeResponse(

        @Schema(description = "Идентификатор записи ref_population (для перехода к детальному просмотру).", example = "1")
        Long id,

        @Schema(description = "Уровень административной иерархии.")
        PopulationGeoType geoType,

        @Schema(description = "Код территории по МХОБТ/СОАТО.", example = "1703")
        Integer soatoId,

        @Schema(description = "Код узла: \"UZ\" для республики, ref_region.code для области, ref_district.code для района.",
                example = "UZ-AN")
        String code,

        @Schema(description = "Наименование территории на языке текущей локали.", example = "Андижанская область")
        String name,

        @Schema(description = "Год.", example = "2025")
        Integer year,

        @Schema(description = "Численность постоянного населения, человек.", example = "3268000")
        Long population,

        @Schema(description = "Источник данных.")
        PopulationSource source,

        @Schema(description = "Есть ли дочерние узлы (области у республики, районы у области).", example = "true")
        boolean hasChildren
) {
}
