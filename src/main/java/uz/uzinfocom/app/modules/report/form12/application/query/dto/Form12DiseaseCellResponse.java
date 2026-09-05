package uz.uzinfocom.app.modules.report.form12.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-болезнь в строке отчёта «Form 12 (по территориям)»: подтверждённые "
        + "(status = APPROVED) случаи форм №058 + №058-1 для одной нозологической формы (записи справочника "
        + "ручных отчётов с тегом FORM_12) в данном узле географии, за выбранный период. Один период — без "
        + "сравнения год назад.")
public record Form12DiseaseCellResponse(
        @Schema(description = "Id записи справочника ручных отчётов (нозологическая форма).")
        long manualReportId,

        @Schema(description = "«Qator kodi» — код записи справочника ручных отчётов.")
        String code,

        @Schema(description = "Локализованное наименование нозологической формы.")
        String name,

        @Schema(description = "«Xalqaro kasalliklar tasnifi kodi» — краткое отображаемое наименование (shortName).")
        String icd10Display,

        @Schema(description = "Всего.")
        long total,

        @Schema(description = "Дети до 14 лет.")
        long under14,

        @Schema(description = "Дети до 18 лет.")
        long under18
) {
}
