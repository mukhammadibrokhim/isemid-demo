package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Один наблюдённый интервал обучающего ряда (gap-filled — интервалы без "
        + "случаев присутствуют с actual = 0).")
public record ForecastHistoryPointResponse(
        @Schema(description = "Дата начала интервала (для WEEK — понедельник ISO-недели, для MONTH — 1-е число).")
        LocalDate periodStart,

        @Schema(description = "Дата конца интервала включительно.")
        LocalDate periodEnd,

        @Schema(description = "Фактическое число извещений (формы №058 + №058-1) в этом интервале.")
        long actual
) {
}
