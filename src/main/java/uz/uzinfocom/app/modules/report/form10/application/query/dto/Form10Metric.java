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

        @Schema(description = "Прирост абсолютного показателя, % — ((curr - prev) / prev) * 100 "
                + "(prev = 0: curr = 0 → 0, curr > 0 → 100).")
        double absGrowthPercent,

        @Schema(description = "Intensiv ko'rsatkich (на koef населения) за тот же период прошлого года — "
                + "absPreviousYear * koef / население территории за прошлый год.")
        double intensivePreviousYear,

        @Schema(description = "Intensiv ko'rsatkich (на koef населения) за выбранный период текущего года.")
        double intensiveCurrentYear,

        @Schema(description = "Прирост интенсивного показателя, %.")
        double intensiveGrowthPercent
) {
}
