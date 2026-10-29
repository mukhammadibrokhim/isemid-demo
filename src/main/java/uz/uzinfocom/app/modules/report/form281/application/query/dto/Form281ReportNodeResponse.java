package uz.uzinfocom.app.modules.report.form281.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка отчёта «Form 28.1: сведения об отдельных инфекционных и паразитарных "
        + "заболеваниях». На корневом уровне — одна нозологическая форма (запись справочника ручных "
        + "отчётов с тегом FORM_28_1); при раскрытии строки — один узел географии "
        + "(регион/район/организация) для этой же нозологической формы, либо итоговая строка «Jami». "
        + "Числа — подтверждённые (status = APPROVED, deleted = false) случаи форм №058 + №058-1, чей "
        + "заключительный код МКБ-10 (final_icd10_code) входит в набор кодов записи, за выбранный период.")
public record Form281ReportNodeResponse(
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

        @Schema(description = "«Qishloq aholisida — Jami 1-ustundan» — всего из сельского населения "
                + "(patient.population_type_code = VILLAGE_RESIDENT).")
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
