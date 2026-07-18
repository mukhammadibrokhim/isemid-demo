package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество карт по дате назначения (создания).")
public record CardDailyCountResponse(
        @Schema(description = "Дата (день) назначения карты.")
        LocalDate date,

        @Schema(description = "Количество карт, назначенных в этот день.")
        long count
) {
}
