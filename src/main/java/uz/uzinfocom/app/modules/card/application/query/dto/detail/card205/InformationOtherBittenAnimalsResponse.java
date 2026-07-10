package uz.uzinfocom.app.modules.card.application.query.dto.detail.card205;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о другом животном, пострадавшем от того же источника заражения.")
public record InformationOtherBittenAnimalsResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код категории укушенного животного (по справочнику).")
        String bittenAnimalCategoryCode,

        @Schema(description = "Дата и время укуса.")
        LocalDateTime bittenDateTime,

        @Schema(description = "Место, где животное было укушено.")
        String whereAnimalBitten
) {
}
