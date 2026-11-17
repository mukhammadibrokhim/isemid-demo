package uz.uzinfocom.app.modules.report.form6.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 6: возрастная "
        + "структура инфекционных и паразитарных заболеваний», с сравнением текущего периода "
        + "и того же периода год назад.")
public record Form6ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Количество первичных извещений за тот же период год назад (\"O'tgan yil\").")
        long previousYear,

        @Schema(description = "Количество первичных извещений за выбранный период (\"Joriy yil\").")
        long currentYear,

        @Schema(description = "Разница currentYear - previousYear (\"O'sish / Kamayish\").")
        long delta
) {
}
