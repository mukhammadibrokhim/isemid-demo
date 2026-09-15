package uz.uzinfocom.app.modules.reference.application.population.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationSource;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;

import java.util.List;

@Schema(description = "Полная информация по записи численности населения: поля записи, наименования "
        + "территорий по текущей локали, все годы этой территории и аудит создания/изменения.")
public record PopulationDetailResponse(

        @Schema(description = "Внутренний идентификатор записи.", example = "1")
        Long id,

        @Schema(description = "Уровень административной иерархии.")
        PopulationGeoType geoType,

        @Schema(description = "Код территории по МХОБТ/СОАТО.", example = "1703")
        Integer soatoId,

        @Schema(description = "Код узла: \"UZ\" / ref_region.code / ref_district.code.", example = "UZ-AN")
        String code,

        @Schema(description = "Наименование территории на языке текущей локали.", example = "Андижанская область")
        String name,

        @Schema(description = "Код области.", example = "UZ-AN")
        String regionCode,

        @Schema(description = "Наименование области на языке текущей локали.", example = "Андижанская область")
        String regionName,

        @Schema(description = "Код района.", example = "AN-203")
        String districtCode,

        @Schema(description = "Наименование района на языке текущей локали.", example = "Андижанский район")
        String districtName,

        @Schema(description = "Год запрошенной записи.", example = "2025")
        Integer year,

        @Schema(description = "Численность за год запрошенной записи, человек.", example = "3268000")
        Long population,

        @Schema(description = "Источник данных запрошенной записи.")
        PopulationSource source,

        @Schema(description = "Признак мягкого удаления запрошенной записи.", example = "false")
        Boolean deleted,

        @Schema(description = "Все годы этой территории (soatoId) со значениями численности, по убыванию года.")
        List<PopulationYearValueResponse> years,

        @Schema(description = "Аудит: кто и когда создал / последним изменил запись.")
        AuditResponse audit
) {
}
