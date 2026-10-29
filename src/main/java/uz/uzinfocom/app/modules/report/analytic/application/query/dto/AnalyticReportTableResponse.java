package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReportStatus;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Analitik hisobotlar jadvali uchun bitta qator.")
public record AnalyticReportTableResponse(
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
