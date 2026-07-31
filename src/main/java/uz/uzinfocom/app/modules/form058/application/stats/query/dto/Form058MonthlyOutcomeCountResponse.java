package uz.uzinfocom.app.modules.form058.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество форм №058, зарегистрированных за один календарный месяц, с разбивкой "
        + "по итоговому статусу (CANCELED/APPROVED) — для месячной динамики дашборда.")
public record Form058MonthlyOutcomeCountResponse(
        @Schema(description = "Первый день месяца.")
        LocalDate periodStart,

        @Schema(description = "Общее количество форм, зарегистрированных за этот месяц.")
        long total,

        @Schema(description = "Из них — со статусом CANCELED.")
        long canceledCount,

        @Schema(description = "Из них — со статусом APPROVED.")
        long approvedCount
) {
}
