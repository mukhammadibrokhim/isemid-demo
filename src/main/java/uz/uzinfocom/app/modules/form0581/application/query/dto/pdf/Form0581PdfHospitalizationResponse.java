package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о госпитализации пациента для печатной формы №058-1.")
public record Form0581PdfHospitalizationResponse(
        @Schema(description = "Дата и время госпитализации пациента.")
        LocalDateTime hospitalizedAt,

        @Schema(description = "Наименование организации госпитализации пациента.")
        String hospitalOrganizationName
) {
}
