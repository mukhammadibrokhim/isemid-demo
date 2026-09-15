package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Одна точка ряда динамики «Statistika» — один временной интервал (день / неделя / "
        + "месяц). Формы №058, №058-1 и №129 представлены отдельно и никогда не суммируются. У форм "
        + "№058/№058-1 — подтверждённые (status = APPROVED) и первичные (status not in (APPROVED, CANCELED)) "
        + "случаи; у формы №129 — общее число извещений.")
public record StatisticsSeriesPointResponse(
        @Schema(description = "Дата начала интервала (включительно).")
        LocalDate bucketStart,

        @Schema(description = "Дата конца интервала (включительно).")
        LocalDate bucketEnd,

        @Schema(description = "Форма №058 — подтверждённые случаи за интервал.")
        long form058Confirmed,

        @Schema(description = "Форма №058 — первичные случаи за интервал.")
        long form058Primary,

        @Schema(description = "Форма №058-1 — подтверждённые случаи за интервал.")
        long form0581Confirmed,

        @Schema(description = "Форма №058-1 — первичные случаи за интервал.")
        long form0581Primary,

        @Schema(description = "Форма №129 — всего извещений за интервал.")
        long form129Total
) {
}
