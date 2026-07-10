package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сведения о наблюдении за контактным лицом.")
public record ContactMonitoringResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "ФИО контактного лица.")
        String fullName,

        @Schema(description = "Дата рождения контактного лица.")
        LocalDate birthDate,

        @Schema(description = "Возраст контактного лица.")
        Integer age,

        @Schema(description = "Код степени родства/отношения с пациентом (по справочнику).")
        String relationCode,

        @Schema(description = "Место работы или учёбы контактного лица.")
        String workplaceOrStudyPlace,

        @Schema(description = "Учреждение, получившее уведомление о контактном лице.")
        String notificationReceiver,

        @Schema(description = "Дата установления диагноза у контактного лица (при выявлении заболевания).")
        LocalDate diagnosisDate,

        @Schema(description = "Код текущего статуса наблюдения за контактным лицом (по справочнику).")
        String contactStatusCode
) {
}
