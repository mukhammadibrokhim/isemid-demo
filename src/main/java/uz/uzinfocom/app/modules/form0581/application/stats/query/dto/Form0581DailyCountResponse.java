package uz.uzinfocom.app.modules.form0581.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество форм №058-1 по дате создания.")
public record Form0581DailyCountResponse(
        @Schema(description = "Дата (день) создания формы.")
        LocalDate date,

        @Schema(description = "Количество форм, созданных в этот день.")
        long count
) {
}
