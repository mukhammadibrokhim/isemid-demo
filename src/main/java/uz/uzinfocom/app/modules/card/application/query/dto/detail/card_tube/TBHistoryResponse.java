package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сведения о ранее перенесённом случае туберкулёза.")
public record TBHistoryResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Место, где произошло заражение.")
        String infectionLocation,

        @Schema(description = "Дата заражения.")
        LocalDate infectionDate,

        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Группа диспансерного учёта при регистрации случая.")
        String registrationGroup
) {
}
