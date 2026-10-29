package uz.uzinfocom.app.modules.report.form32.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Данные для обновления ручной записи Shakl №3-2 (санитарные проверки, нарушения, "
        + "должностные лица, приостановленные объекты).")
public record Form32EntryUpdateRequest(
        @Schema(description = "Начало периода отчёта.", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form32_entry.from.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form32_entry.to.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "Tekshirilgan obyektlar — Jami.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer inspectedTotalCount,

        @Schema(description = "Tekshirilgan obyektlar — shu jumladan MTM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer inspectedMtmCount,

        @Schema(description = "Tekshirilgan obyektlar — shu jumladan Maktab.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer inspectedSchoolCount,

        @Schema(description = "Tekshirilgan obyektlar — shu jumladan DPM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer inspectedDpmCount,

        @Schema(description = "Tekshirilgan obyektlar — shu jumladan Boshqa.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer inspectedOtherCount,

        @Schema(description = "Aniqlangan kamchiliklar — Jami.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer deficiencyTotalCount,

        @Schema(description = "Aniqlangan kamchiliklar — shu jumladan MTM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer deficiencyMtmCount,

        @Schema(description = "Aniqlangan kamchiliklar — shu jumladan Maktab.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer deficiencySchoolCount,

        @Schema(description = "Aniqlangan kamchiliklar — shu jumladan DPM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer deficiencyDpmCount,

        @Schema(description = "Aniqlangan kamchiliklar — shu jumladan Boshqa.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer deficiencyOtherCount,

        @Schema(description = "Mansabdor shaxslar — Jami.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer officialTotalCount,

        @Schema(description = "Mansabdor shaxslar — shu jumladan MTM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer officialMtmCount,

        @Schema(description = "Mansabdor shaxslar — shu jumladan Maktab.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer officialSchoolCount,

        @Schema(description = "Mansabdor shaxslar — shu jumladan DPM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer officialDpmCount,

        @Schema(description = "Mansabdor shaxslar — shu jumladan Boshqa.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer officialOtherCount,

        @Schema(description = "To'xtatilgan obyektlar — Jami.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer suspendedTotalCount,

        @Schema(description = "To'xtatilgan obyektlar — shu jumladan MTM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer suspendedMtmCount,

        @Schema(description = "To'xtatilgan obyektlar — shu jumladan Maktab.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer suspendedSchoolCount,

        @Schema(description = "To'xtatilgan obyektlar — shu jumladan DPM.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer suspendedDpmCount,

        @Schema(description = "To'xtatilgan obyektlar — shu jumladan Boshqa.", example = "0")
        @PositiveOrZero(message = "{report.form32_entry.count.positive_or_zero}")
        Integer suspendedOtherCount
) {
}
