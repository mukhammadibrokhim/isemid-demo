package uz.uzinfocom.app.modules.report.form10.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One "2025 / 2026 / o'sish-pasayish %" triple of the "Form 10" table, for a
 * single population slice (either the whole population or the under-14 cut) of
 * a single column block (either "Joriy davr" or "Yig'ma"). Absolute case
 * counts and the intensive rate (per {@code koef} of that year's territory
 * population) sit side by side with their year-over-year growth.
 */
public record Form10Metric(
        @Schema(description = "Absolyut ko'rsatkich — случаев за тот же период прошлого года.")
        long absPreviousYear,

        @Schema(description = "Absolyut ko'rsatkich — случаев за выбранный период текущего года.")
        long absCurrentYear,

        @Schema(description = "Прирост абсолютного показателя — |curr - prev|, разница в числе случаев "
                + "(НЕ проценты — в отличие от intensiveGrowthPercent ниже).")
        double absGrowthPercent,

        @Schema(description = "Intensiv ko'rsatkich (на koef населения) за тот же период прошлого года — "
                + "absPreviousYear * koef / население территории за прошлый год.")
        double intensivePreviousYear,

        @Schema(description = "Intensiv ko'rsatkich (на koef населения) за выбранный период текущего года.")
        double intensiveCurrentYear,

        @Schema(description = "Прирост интенсивного показателя. Обычно строка с числом-процентом "
                + "(напр. \"-12.22\"), но при изменении в 2 раза и более — текст вида \"X marta\"/"
                + "\"-X marta\" (\"в X раз\") вместо процента — см. Form10ReportQueryService#growthDisplay.")
        String intensiveGrowthPercent
) {
}
