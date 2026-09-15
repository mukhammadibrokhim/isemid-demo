package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сводка по прогнозу: какой ряд обучался, какая модель выбрана, куда указывает "
        + "тренд и есть ли сигнал превышения эпидемического порога.")
public record ForecastSummaryResponse(
        @Schema(description = "Код узла географии (код региона/района, id организации или \"UZ\").")
        String territoryCode,

        @Schema(description = "Локализованное название узла географии.")
        String territoryName,

        @Schema(description = "Фильтр по коду МКБ-10, если задан; иначе null (все болезни).")
        String diagnosisCode,

        @Schema(description = "Единица интервала: DAY / WEEK / MONTH.")
        ForecastBucketUnit bucket,

        @Schema(description = "Начало обучающего ряда (после применения look-back по умолчанию, если from не задан).")
        LocalDate trainingStart,

        @Schema(description = "Конец обучающего ряда включительно.")
        LocalDate trainingEnd,

        @Schema(description = "Число интервалов в обучающем ряде.")
        int trainingBuckets,

        @Schema(description = "Суммарное число извещений в обучающем ряде.")
        long trainingTotal,

        @Schema(description = "Средняя за интервал по обучающему ряду.")
        double trainingMeanPerBucket,

        @Schema(description = "Фактически применённая модель прогноза (AUTO уже разрешён в конкретную).")
        ForecastMethod method,

        @Schema(description = "Оценка изменения уровня за один будущий интервал (наклон тренда). "
                + "> 0 — рост, < 0 — снижение, ≈ 0 — стабильно.")
        double trendPerBucket,

        @Schema(description = "Сумма точечных прогнозов по всему горизонту.")
        long forecastTotal,

        @Schema(description = "Число будущих интервалов с прогнозом выше эпидемического порога.")
        int alertBuckets,

        @Schema(description = "Дата начала интервала с наибольшим точечным прогнозом (пик), либо null при пустом горизонте.")
        LocalDate peakPeriodStart
) {
}
