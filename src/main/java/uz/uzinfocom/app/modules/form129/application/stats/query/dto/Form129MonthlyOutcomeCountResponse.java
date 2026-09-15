package uz.uzinfocom.app.modules.form129.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество форм №129, зарегистрированных за один календарный месяц, с разбивкой по "
        + "итоговому статусу (CANCELED/ACCEPTED) — для месячной динамики дашборда.")
public record Form129MonthlyOutcomeCountResponse(
        @Schema(description = "Первый день месяца.")
        LocalDate periodStart,

        @Schema(description = "Общее количество форм, зарегистрированных за этот месяц.")
        long total,

        @Schema(description = "Из них — со статусом CANCELED.")
        long canceledCount,

        @Schema(description = "Из них — со статусом ACCEPTED.")
        long acceptedCount
) {
}
