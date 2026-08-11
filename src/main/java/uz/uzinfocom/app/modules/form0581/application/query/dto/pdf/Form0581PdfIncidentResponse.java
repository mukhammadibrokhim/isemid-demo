package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о происшествии (укус/царапина/ослюнение животным) для печатной формы №058-1.")
public record Form0581PdfIncidentResponse(
        @Schema(description = "Дата и время получения укуса/травмы.")
        LocalDateTime injuryDateTime,

        @Schema(description = "Дата и время обращения в травматологический пункт (ДПУ).")
        LocalDateTime dpuVisitDateTime,

        @Schema(description = "Наименование региона, где произошёл укус.")
        String injuryRegionName,

        @Schema(description = "Наименование района, где произошёл укус.")
        String injuryDistrictName,

        @Schema(description = "Адрес места происшествия.")
        String injuryAddress
) {
}
