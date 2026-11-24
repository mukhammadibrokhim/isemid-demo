package uz.uzinfocom.app.modules.report.form11.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One column block of the "Form 11" table — either <b>"Joriy davr"</b> (the
 * selected {@code ReportPeriod}'s month span) or <b>"Yig'ma"</b> (January
 * through the end of that span). Each block carries the whole-population
 * triple plus the urban / rural / under-18 triples — all four divide by the
 * same node territory population (matches {@code Form10Block}'s "no separate
 * population source" design; there is no age- or residence-segmented {@code
 * ref_population} figure).
 */
public record Form11Block(
        @Schema(description = "Umumiy aholi — o'tgan yil / joriy yil / o'sish-pasayish (absolyut + intensiv).")
        Form11Metric total,

        @Schema(description = "Shahar aholisi — o'tgan yil / joriy yil / o'sish-pasayish.")
        Form11Metric city,

        @Schema(description = "Qishloq aholisi — o'tgan yil / joriy yil / o'sish-pasayish.")
        Form11Metric rural,

        @Schema(description = "18 yoshgacha bolalar — o'tgan yil / joriy yil / o'sish-pasayish. Интенсивный "
                + "делится на ту же общую численность населения территории (отдельной детской численности нет).")
        Form11Metric child
) {
}
