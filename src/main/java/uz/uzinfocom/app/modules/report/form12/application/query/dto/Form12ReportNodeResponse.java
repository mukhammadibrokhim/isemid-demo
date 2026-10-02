package uz.uzinfocom.app.modules.report.form12.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка отчёта «Form 12: инфекционные и паразитарные заболевания по "
        + "нозологическим формам». На корневом уровне — одна нозологическая форма (запись справочника "
        + "ручных отчётов с типом FORM_12); при раскрытии строки — один узел географии "
        + "(регион/район/организация) для этой же нозологической формы. Три метрики "
        + "(всего / до 14 лет / до 18 лет), каждая за выбранный период рядом с тем же периодом год назад "
        + "и разницей.")
public record Form12ReportNodeResponse(
        @Schema(description = "Код строки: id записи справочника (нозологическая форма) либо код "
                + "региона/района / id организации (география).")
        String code,

        @Schema(description = "Локализованное наименование строки (наименование нозологической формы "
                + "либо наименование узла географии).")
        String name,

        @Schema(description = "«Qator kodi» — код записи справочника ручных отчётов. null для строк географии "
                + "и итоговой строки.")
        String rowCode,

        @Schema(description = "«Xalqaro kasalliklar tasnifi kodi» — краткое отображаемое наименование "
                + "(shortName) записи справочника. null для строк географии и итоговой строки.")
        String icd10Display,

        @Schema(description = "Есть ли у строки более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Всего — за тот же период год назад (\"O'tgan yil\").")
        long totalPreviousYear,

        @Schema(description = "Всего — за выбранный период (\"Joriy yil\").")
        long totalCurrentYear,

        @Schema(description = "Всего — разница currentYear - previousYear.")
        long totalDelta,

        @Schema(description = "Дети до 14 лет — за тот же период год назад.")
        long under14PreviousYear,

        @Schema(description = "Дети до 14 лет — за выбранный период.")
        long under14CurrentYear,

        @Schema(description = "Дети до 14 лет — разница.")
        long under14Delta,

        @Schema(description = "Дети до 18 лет — за тот же период год назад.")
        long under18PreviousYear,

        @Schema(description = "Дети до 18 лет — за выбранный период.")
        long under18CurrentYear,

        @Schema(description = "Дети до 18 лет — разница.")
        long under18Delta
) {
}
