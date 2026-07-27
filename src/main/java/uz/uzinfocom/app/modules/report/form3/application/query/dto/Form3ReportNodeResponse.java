package uz.uzinfocom.app.modules.report.form3.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 3: сравнительный анализ "
        + "заболеваемости по годам».")
public record Form3ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Первичные случаи за тот же период год назад (o'tgan yil).")
        long previousYearTotal,

        @Schema(description = "Первичные случаи за выбранный период (joriy yil).")
        long currentYearTotal,

        @Schema(description = "Разница: currentYearTotal - previousYearTotal (o'sish/kamayish).")
        long change
) {
}
