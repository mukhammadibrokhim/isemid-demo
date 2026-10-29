package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Одна строка рейтинга «какие болезни могут вырасти» для выбранного узла — прогноз "
        + "по каждому МКБ-10 коду, встречавшемуся в обучающем окне, отсортированный по уровню риска.")
public record ForecastDiseaseRiskResponse(
        @Schema(description = "Код МКБ-10 (текущий — заключительный, если есть, иначе первичный).")
        String diagnosisCode,

        @Schema(description = "Локализованное название диагноза.")
        String diagnosisName,

        @Schema(description = "Категория риска: HIGH — есть будущие интервалы выше эпидемического порога, "
                + "MEDIUM — порог не превышен, но тренд растёт, LOW — ни то, ни другое.")
        ForecastRiskLevel riskLevel,

        @Schema(description = "Фактически применённая модель прогноза для этой болезни.")
        ForecastMethod method,

        @Schema(description = "Суммарное число извещений в обучающем ряде по этой болезни.")
        long trainingTotal,

        @Schema(description = "Факт последнего наблюдённого интервала.")
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
