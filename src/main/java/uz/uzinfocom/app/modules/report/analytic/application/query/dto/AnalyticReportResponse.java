package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReportStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Analitik hisobotning to'liq ma'lumotlari — qayta ochib tahrirlash uchun.")
public record AnalyticReportResponse(
        @Schema(description = "Ichki identifikator.", example = "1")
        Long id,

        @Schema(description = "Hisobot nomi.", example = "01.09.2026 - 05.09.2026 Toshkent shahri, A09 - ...")
        String name,

        @Schema(description = "Holati.", example = "FINAL")
        AnalyticReportStatus status,

        @Schema(description = "Davr boshlanishi.", example = "2026-09-01")
        LocalDate fromDate,

        @Schema(description = "Davr tugashi.", example = "2026-09-05")
        LocalDate toDate,

        @Schema(description = "Tanlangan hudud kodlari.")
        Set<String> regionCodes,

        @Schema(description = "Tanlangan KXK-10 kodlari.")
        Set<String> icd10Codes,

        @Schema(description = "Nisbiylik koeffitsiyenti.", example = "100000")
        Long koef,

        @Schema(description = "Muharrirdan saqlangan tahrirlanuvchi (rich text) mazmun.")
        String content,

        @Schema(description = "Yaratgan tashkilotning ichki identifikatori.", example = "10")
        Long organizationId,

        @Schema(description = "Yaratgan tashkilot nomi.", example = "Toshkent shahar SES")
        String organizationName,

        @Schema(description = "Yaratilgan sana.")
        Instant createdAt,

        @Schema(description = "Oxirgi yangilangan sana.")
        Instant updatedAt
) {
}
