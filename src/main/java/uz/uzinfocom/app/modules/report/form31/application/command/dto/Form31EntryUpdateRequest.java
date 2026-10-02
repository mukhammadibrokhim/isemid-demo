package uz.uzinfocom.app.modules.report.form31.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Данные для обновления ручной записи Shakl №3-1 (ILI/SARI, вакцинация против гриппа).")
public record Form31EntryUpdateRequest(
        @Schema(description = "Начало периода отчёта.", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form31_entry.from.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form31_entry.to.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "Grippga o'xshash kasallik (ГПЗ) holatlari soni.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer iliCasesCount,

        @Schema(description = "O'RI (o'tkir respirator infeksiyasi) holatlari soni.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer ariCasesCount,

        @Schema(description = "O'tkir zotiljam holatlari soni.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer pneumoniaCasesCount,

        @Schema(description = "OO'RI — Jami (umumiy soni).", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer sariTotalCount,

        @Schema(description = "OO'RI — Ulardan homiladorlar.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer sariPregnantCount,

        @Schema(description = "O'lganlar — Jami (umumiy soni).", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer deathTotalCount,

        @Schema(description = "O'lganlar — Ulardan homiladorlar.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer deathPregnantCount,

        @Schema(description = "Vaktsinatsiya grippga qarshi — Haftalik kasallanish.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer weeklyVaccinationCount,

        @Schema(description = "Vaktsinatsiya grippga qarshi — Mavsum boshidan.", example = "0")
        @PositiveOrZero(message = "{report.form31_entry.count.positive_or_zero}")
        Integer seasonVaccinationCount
) {
}
