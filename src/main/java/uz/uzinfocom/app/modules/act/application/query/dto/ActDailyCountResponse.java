package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество актов по дате назначения (создания).")
public record ActDailyCountResponse(
        @Schema(description = "Дата (день) назначения акта.")
        LocalDate date,

        @Schema(description = "Количество актов, назначенных в этот день.")
        long count
) {
}
