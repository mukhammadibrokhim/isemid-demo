package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Возможный источник заражения туберкулёзным контактом.")
public record InfectionSourceResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код туберкулёзного контакта (по справочнику).")
        String tbContactCode,

        @Schema(description = "ФИО предполагаемого источника заражения.")
        String fullName,

        @Schema(description = "Код степени родства с предполагаемым источником заражения (по справочнику).")
        String relationDegreeCode,

        @Schema(description = "Длительность контакта с источником заражения.")
        String contactDuration
) {
}
