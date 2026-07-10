package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о госпитализации пациента.")
public record Form0581HospitalizationDetailResponse(
        @Schema(description = "Дата и время госпитализации пациента.")
        LocalDateTime hospitalizedAt,

        @Schema(description = "Идентификатор организации госпитализации пациента.")
        Long hospitalOrganizationId
) {
}
