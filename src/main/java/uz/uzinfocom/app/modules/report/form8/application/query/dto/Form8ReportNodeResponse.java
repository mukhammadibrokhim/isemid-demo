package uz.uzinfocom.app.modules.report.form8.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 8: инфекционные и "
        + "паразитарные заболевания по социальному составу», с сравнением текущего периода "
        + "и того же периода год назад.")
public record Form8ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Количество подтверждённых извещений за тот же период год назад (\"O'tgan yil\").")
        long previousYear,

        @Schema(description = "Количество подтверждённых извещений за выбранный период (\"Joriy yil\").")
        long currentYear,

        @Schema(description = "Разница currentYear - previousYear (\"O'sish / Kamayish\").")
        long delta
) {
}
