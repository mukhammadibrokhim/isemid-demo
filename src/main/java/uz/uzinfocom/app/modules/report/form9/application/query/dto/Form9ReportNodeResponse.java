package uz.uzinfocom.app.modules.report.form9.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 9: сравнительные "
        + "данные по инфекционным заболеваниям», с двумя метриками (зарегистрировано по первичному "
        + "извещению; госпитализировано) — каждая за выбранный период рядом с тем же периодом год назад.")
public record Form9ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Зарегистрировано по первичному извещению за тот же период год назад (\"O'tgan yil\").")
        long registeredPreviousYear,

        @Schema(description = "Зарегистрировано по первичному извещению за выбранный период (\"Joriy yil\").")
        long registeredCurrentYear,

        @Schema(description = "Разница registeredCurrentYear - registeredPreviousYear (\"Taqqoslash (+/-)\").")
        long registeredDelta,

        @Schema(description = "Госпитализировано за тот же период год назад (\"O'tgan yil\").")
        long hospitalizedPreviousYear,

        @Schema(description = "Госпитализировано за выбранный период (\"Joriy yil\").")
        long hospitalizedCurrentYear,

        @Schema(description = "Разница hospitalizedCurrentYear - hospitalizedPreviousYear (\"Taqqoslash (+/-)\").")
        long hospitalizedDelta
) {
}
