package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Фактор качества воздуха (AIR_CONTROL) рядом с прогнозом заболеваемости: уровень загрязнителя "
        + "за обучающее окно, его связь с числом извещений и сигнал. Точечный прогноз и пороги НЕ корректируются: "
        + "история измерений воздуха коротка, и причинно-следственная поправка была бы выдумкой. Это контекст "
        + "для оценки риска.")
public record ForecastAirFactorResponse(
        @Schema(description = "Загрязнитель.", example = "PM2.5") String parameter,
        @Schema(description = "Единица уровней.", example = "µg/m³") String unit,
        @Schema(description = "Норма загрязнителя (µg/m³); null — нормы нет.") BigDecimal norm,
        @Schema(description = "Число интервалов обучающего ряда.") int trainingBuckets,
        @Schema(description = "Из них интервалов с измерениями воздуха. История копится только с включения синхронизации.")
        int bucketsWithAirData,
        @Schema(description = "Средний уровень за последние (до 4) интервалов с измерениями; null — измерений нет.")
        Double recentAverage,
        @Schema(description = "recentAverage / норма; > 1 — выше нормы. null — нет измерений или нормы.")
        Double recentRatio,
        @Schema(description = "Число интервалов со средним уровнем выше нормы.") int exceededBuckets,
        @Schema(description = "Корреляция Пирсона между уровнем воздуха и числом извещений по интервалам; "
                + "null — не определена (меньше 3 пар или ряд не меняется).")
        Double correlation,
        @Schema(description = "Число пар интервалов в корреляции.") int correlationPairs,
        @Schema(description = "Пар не меньше 14 — коэффициент достоверен, иначе ориентировочный.")
        boolean correlationReliable,
        @Schema(description = "NO_DATA — нет измерений/нормы; NEUTRAL — недавний уровень в норме; "
                + "WATCH — недавний уровень выше нормы; ELEVATED — выше нормы и достоверная корреляция >= 0.3.")
        Signal signal
) {

    public enum Signal {
        NO_DATA,
        NEUTRAL,
        WATCH,
        ELEVATED
    }
}
