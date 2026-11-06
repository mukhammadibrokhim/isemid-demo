package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Одна строка географической разбивки прогноза — компактная сводка прогноза для "
        + "одного узла (регион / район / организация / «Jami»). Полный ряд history[]/forecast[] и "
        + "график берутся отдельным вызовом /series для этого узла.")
public record ForecastNodeResponse(
        @Schema(description = "Код узла: код региона/района, id организации, \"UZ\" (республика) или \"TOTAL\" (Jami).")
        String code,

        @Schema(description = "Локализованное название узла.")
        String name,

        @Schema(description = "Есть ли более глубокий уровень (показывать стрелку раскрытия). У \"Jami\" всегда false.")
        boolean hasChildren,

        @Schema(description = "Фактически применённая модель прогноза для этого узла.")
        ForecastMethod method,

        @Schema(description = "Суммарное число извещений в обучающем ряде узла.")
        long trainingTotal,

        @Schema(description = "Факт последнего наблюдённого интервала обучающего ряда.")
        long lastActual,

        @Schema(description = "Точечный прогноз на ближайший будущий интервал.")
        long nextPredicted,

        @Schema(description = "Сумма точечных прогнозов по всему горизонту.")
        long forecastTotal,

        @Schema(description = "Оценка изменения уровня за один будущий интервал (наклон). > 0 рост, < 0 спад.")
        double trendPerBucket,

        @Schema(description = "Число будущих интервалов с прогнозом выше эпидемического порога.")
        int alertBuckets,

        @Schema(description = "Дата начала интервала с наибольшим точечным прогнозом, либо null.")
        LocalDate peakPeriodStart
) {
}
