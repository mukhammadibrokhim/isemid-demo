package uz.uzinfocom.app.modules.report.airhealth.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Havo va nafas kasalliklari: hududlar kesimida jadval va jami qator.")
public record AirHealthTerritoriesResponse(
        @Schema(description = "Ifloslantiruvchi kodi.", example = "PM2.5") String parameter,
        @Schema(description = "Daraja birligi.", example = "µg/m³") String unit,
        @Schema(description = "Ifloslantiruvchining me'yori (µg/m³); me'yori yo'q bo'lsa null.") BigDecimal norm,
        @Schema(description = "Hisoblangan kasalliklar ICD-10 prefiksi.", example = "J") String diagnosisPrefix,
        @Schema(description = "Qatorlar bo'yicha aholi koeffitsiyenti.", example = "100000") int koef,
        LocalDate from,
        LocalDate to,
        @Schema(description = "Hududlar, tasdiqlangan holatlar kamayish tartibida.") List<AirHealthTerritoryResponse> rows,
        @Schema(description = "Jami: holatlar yig'indisi, o'lchovlar bo'yicha o'rtacha/maksimum, aholi yig'indisi.")
        AirHealthTerritoryResponse total
) {
}
