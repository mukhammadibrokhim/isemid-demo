package uz.uzinfocom.app.modules.report.form282.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-болезнь в строке отчёта «Form 28.2 (по территориям)»: подтверждённые "
        + "(status = APPROVED, deleted = false) случаи форм №058 + №058-1 для одной нозологической формы "
        + "(записи справочника ручных отчётов с тегом FORM_28_2) в данном узле географии, за выбранный "
        + "период. Один период — без сравнения год назад.")
public record Form282DiseaseCellResponse(
        @Schema(description = "Id записи справочника ручных отчётов (нозологическая форма).")
        long manualReportId,

        @Schema(description = "«Qator kodi» — код записи справочника ручных отчётов.")
        String code,

        @Schema(description = "Локализованное наименование нозологической формы.")
        String name,

        @Schema(description = "«Kasalliklarning XKT bo'yicha shifri» — краткое отображаемое наименование (shortName).")
        String icd10Display,

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
