package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Analitik hisobot uchun oldindan hisoblash so'rovi — davr, hududlar va tashxislar bo'yicha " +
        "aholi soni, tasdiqlangan holatlar soni va nisbiy ko'rsatkichni oldindan ko'rish uchun.")
public record AnalyticReportComputeRequest(
        @Schema(description = "Davr boshlanishi.", example = "2026-09-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.analytic_report.from.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Davr tugashi.", example = "2026-09-05", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.analytic_report.to.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "Hudud (viloyat/tuman) kodlari.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "{report.analytic_report.region_codes.required}")
        @Size(max = 20, message = "{report.analytic_report.region_codes.max}")
        Set<String> regionCodes,

        @Schema(description = "KXK-10 (ICD-10) tashxis kodlari.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "{report.analytic_report.icd10_codes.required}")
        @Size(max = 50, message = "{report.analytic_report.icd10_codes.max}")
        Set<String> icd10Codes,

        @Schema(description = "Nisbiylik koeffitsiyenti (masalan, 100000 aholiga nisbatan). Berilmasa, 100000 qo'llanadi.", example = "100000")
        @Positive(message = "{report.analytic_report.koef.positive}")
        Long koef
) {
}
