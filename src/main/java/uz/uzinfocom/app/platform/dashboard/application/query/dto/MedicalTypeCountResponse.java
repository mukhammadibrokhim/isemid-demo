package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;

@Schema(description = "Количество медицинских учреждений по медицинскому типу (medicalType), в рамках доступа текущей организации.")
public record MedicalTypeCountResponse(
        @Schema(description = "Медицинский тип учреждения.")
        MedicalType medicalType,

        @Schema(description = "Количество активных учреждений этого типа.")
        long count
) {
}
