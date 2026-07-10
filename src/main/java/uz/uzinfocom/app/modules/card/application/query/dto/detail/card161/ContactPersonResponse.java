package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Контактное лицо, подлежащее наблюдению.")
public record ContactPersonResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "ФИО контактного лица.")
        String fullName,

        @Schema(description = "Возраст контактного лица.")
        String age,

        @Schema(description = "Адрес проживания контактного лица.")
        String address,

        @Schema(description = "Место работы/учёбы и должность контактного лица.")
        String jobTypeAndLocation,

        @Schema(description = "Статус иммунизации контактного лица.")
        String immunizationStatus,

        @Schema(description = "Ограничительные меры, применённые в отношении контактного лица.")
        String restrictionMeasures
) {
}
