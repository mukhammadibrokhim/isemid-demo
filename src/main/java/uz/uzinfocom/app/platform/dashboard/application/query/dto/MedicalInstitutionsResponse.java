package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Медицинские учреждения в рамках доступа текущей организации — текущий снимок, не временной ряд.")
public record MedicalInstitutionsResponse(
        @Schema(description = "Общее количество активных учреждений.")
        long total,

        @Schema(description = "Разбивка по медицинскому типу (medicalType).")
        List<MedicalTypeCountResponse> byMedicalType,

        @Schema(description = "Разбивка по уровню организации (levelType).")
        List<LevelTypeCountResponse> byLevelType
) {
}
