package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Один спрогнозированный будущий интервал: точечная оценка, ~95% интервал "
        + "прогноза и эпидемический порог («endemic channel») для этого сезонного момента.")
public record ForecastPredictionPointResponse(
        @Schema(description = "Дата начала будущего интервала.")
        LocalDate periodStart,

        @Schema(description = "Дата конца будущего интервала включительно.")
        LocalDate periodEnd,

        @Schema(description = "Точечный прогноз числа извещений (округлён до целого, ≥ 0).")
        long predicted,

        @Schema(description = "Нижняя граница ~95% интервала прогноза (≥ 0).")
        long lowerBound,

        @Schema(description = "Верхняя граница ~95% интервала прогноза.")
        long upperBound,

        @Schema(description = "Эпидемический порог для этого сезонного момента (среднее + 1.96·SD по "
                + "истории того же месяца/недели/дня недели). Округлён вверх.")
        long endemicThreshold,

        @Schema(description = "true, если точечный прогноз превышает эпидемический порог — сигнал "
                + "возможного подъёма выше сезонной нормы.")
        boolean alert
) {
}
