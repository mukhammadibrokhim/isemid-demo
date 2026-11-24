package uz.uzinfocom.app.modules.report.form9.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка (календарный месяц, либо итоговая строка \"Jami\") месячной "
        + "разбивки «Form 9», с обеими метриками за выбранный период и тот же период год назад.")
public record Form9MonthRowResponse(
        @Schema(description = "Код строки: \"1\"..\"12\" для месяцев, \"TOTAL\" для итоговой строки \"Jami\".")
        String monthCode,

        @Schema(description = "Локализованное наименование месяца (или \"Jami\").")
        String monthName,

        @Schema(description = "Зарегистрировано по первичному извещению за тот же период год назад.")
        long registeredPreviousYear,

        @Schema(description = "Зарегистрировано по первичному извещению за выбранный период.")
        long registeredCurrentYear,

        @Schema(description = "Разница registeredCurrentYear - registeredPreviousYear.")
        long registeredDelta,

        @Schema(description = "Госпитализировано за тот же период год назад.")
        long hospitalizedPreviousYear,

        @Schema(description = "Госпитализировано за выбранный период.")
        long hospitalizedCurrentYear,

        @Schema(description = "Разница hospitalizedCurrentYear - hospitalizedPreviousYear.")
        long hospitalizedDelta
) {
}
