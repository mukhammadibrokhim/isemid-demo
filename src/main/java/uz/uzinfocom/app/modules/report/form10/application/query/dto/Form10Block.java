package uz.uzinfocom.app.modules.report.form10.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One column block of the "Form 10" table — either <b>"Joriy davr"</b> (the
 * selected {@code ReportPeriod}'s month span) or <b>"Yig'ma"</b> (January
 * through the end of that span). Each block carries the whole-population
 * triple and the under-14 triple.
 */
public record Form10Block(
        @Schema(description = "Umumiy aholi — 2025 / 2026 / o'sish-pasayish % (absolyut + intensiv).")
        Form10Metric total,

        @Schema(description = "14 yoshgacha bolalar — 2025 / 2026 / o'sish-pasayish %. Интенсивный делится "
                + "на ту же общую численность населения территории (отдельной детской численности нет).")
        Form10Metric child
) {
}
