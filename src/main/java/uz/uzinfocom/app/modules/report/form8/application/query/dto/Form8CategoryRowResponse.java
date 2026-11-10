package uz.uzinfocom.app.modules.report.form8.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка (социальная категория) таблицы социального состава «Form 8», "
        + "с сравнением текущего периода и того же периода год назад.")
public record Form8CategoryRowResponse(
        @Schema(description = "Код социальной категории (TOTAL, NO_ORGANIZED, ORGANIZED, WORKER, ...).")
        String code,

        @Schema(description = "Локализованное наименование категории.")
        String name,

        @Schema(description = "Количество подтверждённых извещений за тот же период год назад (\"O'tgan yil\").")
        long previousYear,

        @Schema(description = "Количество подтверждённых извещений за выбранный период (\"Joriy yil\").")
        long currentYear,

        @Schema(description = "Разница currentYear - previousYear (\"O'sish / Kamayish\").")
        long delta
) {
}
