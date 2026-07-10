package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о происшествии (укус/царапина/ослюнение животным).")
public record Form0581IncidentDetailResponse(
        @Schema(description = "Дата и время получения укуса/травмы.")
        LocalDateTime injuryDateTime,

        @Schema(description = "Дата и время обращения в травматологический пункт (ДПУ).")
        LocalDateTime dpuVisitDateTime,

        @Schema(description = "Код региона, где произошёл укус.")
        String injuryRegionCode,

        @Schema(description = "Код района, где произошёл укус.")
        String injuryDistrictCode,

        @Schema(description = "Адрес места происшествия.")
        String injuryAddress
) {
}
