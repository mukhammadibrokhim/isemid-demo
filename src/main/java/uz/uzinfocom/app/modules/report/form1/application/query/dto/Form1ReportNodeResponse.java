package uz.uzinfocom.app.modules.report.form1.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 1: мониторинг "
        + "инфекционных и паразитарных заболеваний».")
public record Form1ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Подтверждённые случаи (status = APPROVED) за выбранный период по created_at.")
        Form1CountBlockResponse confirmed,

        @Schema(description = "Доля случаев, у которых итоговый диагноз (final_icd10_code) отличается "
                + "от первичного (icd10_code), среди подтверждённых, в процентах.")
        double diagnosisChangePercent,

        @Schema(description = "Первичные (ещё не решённые — не отменённые и не подтверждённые) случаи "
                + "за выбранный период по created_at.")
        Form1CountBlockResponse primary
) {
}
