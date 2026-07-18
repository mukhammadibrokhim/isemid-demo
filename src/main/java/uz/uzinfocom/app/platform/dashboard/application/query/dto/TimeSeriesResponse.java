package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Wraps any dashboard trend (case/card/act dynamics) with its own explicit
 * period, so a client never has to infer the covered date range from
 * documentation or count points — it's right there alongside the data.
 */
@Schema(description = "Временной ряд с явно указанным охваченным периодом.")
public record TimeSeriesResponse(
        @Schema(description = "Первый день охваченного периода (включительно).")
        LocalDate from,

        @Schema(description = "Последний день охваченного периода (включительно) — как правило, сегодняшняя дата.")
        LocalDate to,

        @Schema(description = "Гранулярность точек временного ряда.")
        TimeSeriesGranularity granularity,

        @Schema(description = "Точки временного ряда, упорядоченные по возрастанию периода. Месяцы без "
                + "данных не включаются в список (пропуски, а не нули).")
        List<DynamicsPointResponse> points
) {
}
