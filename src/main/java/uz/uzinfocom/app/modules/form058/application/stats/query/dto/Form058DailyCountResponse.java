package uz.uzinfocom.app.modules.form058.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество форм №058 по дате создания.")
public record Form058DailyCountResponse(
        @Schema(description = "Дата (день) создания формы.")
        LocalDate date,

        @Schema(description = "Количество форм, созданных в этот день.")
        long count
) {
}
