package uz.uzinfocom.app.modules.report.form6.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка (возрастная группа) таблицы возрастной структуры «Form 6», "
        + "с сравнением текущего периода и того же периода год назад.")
public record Form6AgeGroupRowResponse(
        @Schema(description = "Код возрастной группы (например NEWBORN, AGE_1_2, UNDER_18, ...).")
        String code,

        @Schema(description = "Локализованное наименование возрастной группы.")
        String name,

        @Schema(description = "Количество первичных извещений за тот же период год назад (\"O'tgan yil\").")
        long previousYear,

        @Schema(description = "Количество первичных извещений за выбранный период (\"Joriy yil\").")
        long currentYear,

        @Schema(description = "Разница currentYear - previousYear (\"O'sish / Kamayish\").")
        long delta
) {
}
