package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Прогноз заболеваемости по формам №058 + №058-1: обучающий ряд (history), "
        + "будущие интервалы с интервалами прогноза и эпидемическим порогом (forecast) и сводка (summary).")
public record ForecastResponse(
        ForecastSummaryResponse summary,

        @Schema(description = "Наблюдённый обучающий ряд, старые интервалы первыми, gap-filled.")
        List<ForecastHistoryPointResponse> history,

        @Schema(description = "Спрогнозированные будущие интервалы, ближайший первым.")
        List<ForecastPredictionPointResponse> forecast
) {
}
