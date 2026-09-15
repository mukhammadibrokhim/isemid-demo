package uz.uzinfocom.app.modules.report.form282.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка отчёта «Form 28.2: сведения о внутрибольничных инфекциях». На корневом "
        + "уровне — одна нозологическая форма (запись справочника ручных отчётов с тегом FORM_28_2); "
        + "при раскрытии строки — один узел географии (регион/район/организация) для этой же "
        + "нозологической формы, либо итоговая строка «Jami». Числа — подтверждённые (status = APPROVED, "
        + "deleted = false) случаи форм №058 + №058-1, чей заключительный код МКБ-10 (final_icd10_code) "
        + "входит в набор кодов записи, за выбранный период.")
public record Form282ReportNodeResponse(
        @Schema(description = "Код строки: id записи справочника (нозологическая форма) либо код "
                + "региона/района / id организации (география) / \"TOTAL\" для итоговой строки.")
        String code,

        @Schema(description = "Локализованное наименование строки (нозологическая форма либо узел географии).")
        String name,

        @Schema(description = "«Qator kodi» — код записи справочника ручных отчётов. null для строк "
                + "географии и итоговой строки.")
        String rowCode,

        @Schema(description = "«Kasalliklarning XKT bo'yicha shifri» — краткое отображаемое наименование "
                + "(shortName) записи справочника. null для строк географии и итоговой строки.")
        String icd10Display,

        @Schema(description = "Есть ли у строки более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "«Qayd qilingan kasalliklar, jami» — всего зарегистрировано.")
        long total,

        @Schema(description = "«17 yoshgacha bo'lgan bolalarda (17 yoshni qo'shgan holda)» — возраст < 18 лет.")
        long under18,

        @Schema(description = "«1 oygacha» — возраст < 1 месяца.")
        long underOneMonth,

        @Schema(description = "«1 oy 1 yoshgacha» — возраст от 1 месяца до 1 года.")
        long oneMonthToUnderOneYear
) {
}
