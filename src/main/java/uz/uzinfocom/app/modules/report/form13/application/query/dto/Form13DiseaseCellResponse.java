package uz.uzinfocom.app.modules.report.form13.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-болезнь в строке отчёта «Form 13»: подтверждённые (status = APPROVED) "
        + "случаи форм №058 + №058-1 для одной нозологической формы (записи справочника ручных отчётов "
        + "с тегом FORM_13) в данном узле географии — за выбранный период (\"Joriy yil\") и за тот же "
        + "период год назад (\"O'tgan yil\"), каждый в трёх метриках: всего / до 14 лет / до 18 лет.")
public record Form13DiseaseCellResponse(
        @Schema(description = "Id записи справочника ручных отчётов (нозологическая форма).")
        long manualReportId,

        @Schema(description = "«Qator kodi» — код записи справочника ручных отчётов.")
        String code,

        @Schema(description = "Локализованное наименование нозологической формы.")
        String name,

        @Schema(description = "«Xalqaro kasalliklar tasnifi kodi» — краткое отображаемое наименование (shortName).")
        String icd10Display,

        @Schema(description = "Всего — за тот же период год назад (\"O'tgan yil\").")
        long totalPreviousYear,

        @Schema(description = "Всего — за выбранный период (\"Joriy yil\").")
        long totalCurrentYear,

        @Schema(description = "Дети до 14 лет — за тот же период год назад.")
        long under14PreviousYear,

        @Schema(description = "Дети до 14 лет — за выбранный период.")
        long under14CurrentYear,

        @Schema(description = "Дети до 18 лет — за тот же период год назад.")
        long under18PreviousYear,

        @Schema(description = "Дети до 18 лет — за выбранный период.")
        long under18CurrentYear
) {
}
