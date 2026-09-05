package uz.uzinfocom.app.modules.report.form281.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна ячейка-болезнь в строке отчёта «Form 28.1 (по территориям)»: подтверждённые "
        + "(status = APPROVED, deleted = false) случаи форм №058 + №058-1 для одной нозологической формы "
        + "(записи справочника ручных отчётов с тегом FORM_28_1) в данном узле географии, за выбранный "
        + "период. Один период — без сравнения год назад.")
public record Form281DiseaseCellResponse(
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

        @Schema(description = "«Ayollarda» — из них женщины (patient.gender_code = FEMALE).")
        long female,

        @Schema(description = "«17 yoshgacha bo'lgan bolalarda (17 yoshni qo'shgan holda)» — возраст < 18.")
        long under18,

        @Schema(description = "«14 yoshgacha bo'lgan bolalarda (14 yoshni qo'shgan holda)» — возраст < 15.")
        long under15,

        @Schema(description = "«Ulardan 1 yoshgacha bo'lgan bolalarda» — возраст < 1.")
        long under1,

        @Schema(description = "«1-2 yoshgacha (2 yoshni qo'shgan holda)» — возраст 1–2 года включительно.")
        long age1to2,

        @Schema(description = "«3-5 yoshdagilarda» — возраст 3–5 лет включительно.")
        long age3to5,

        @Schema(description = "Сельское население — всего.")
        long ruralTotal,

        @Schema(description = "Сельское население — возраст < 18.")
        long ruralUnder18,

        @Schema(description = "Сельское население — возраст < 15.")
        long ruralUnder15,

        @Schema(description = "Сельское население — возраст < 1.")
        long ruralUnder1,

        @Schema(description = "Сельское население — возраст 1–2 года включительно.")
        long ruralAge1to2,

        @Schema(description = "Сельское население — возраст 3–5 лет включительно.")
        long ruralAge3to5
) {
}
