package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сведения о мониторинге пострадавшего лица.")
public record InfectionMonitoringResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Порядковый номер записи в списке.")
        Integer sequentialNumber,

        @Schema(description = "Фамилия пострадавшего.")
        String lastName,

        @Schema(description = "Имя пострадавшего.")
        String firstName,

        @Schema(description = "Отчество пострадавшего.")
        String middleName,

        @Schema(description = "Код пола пострадавшего (по справочнику).")
        String genderCode,

        @Schema(description = "Дата рождения пострадавшего.")
        LocalDate birthDate,

        @Schema(description = "Адрес проживания пострадавшего.")
        String address,

        @Schema(description = "Профессия пострадавшего.")
        String profession,

        @Schema(description = "Дата обращения за медицинской помощью.")
        LocalDate applicationDate,

        @Schema(description = "Дата подтверждения диагноза.")
        LocalDate confirmationDate,

        @Schema(description = "Предполагаемое место заражения.")
        String possibleInfectionLocation,

        @Schema(description = "Предполагаемый фактор заражения.")
        String possibleInfectionFactor,

        @Schema(description = "Предполагаемая дата заражения.")
        LocalDate possibleInfectionDate
) {
}
