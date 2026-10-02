package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Analitik hisobot uchun oldindan hisoblangan natija — tahrirlanuvchi matn muharririga " +
        "joylash uchun frontendga qaytariladi.")
public record AnalyticReportComputeResponse(
        @Schema(description = "Qo'llanilgan koeffitsiyent.", example = "100000")
        long koef,

        @Schema(description = "So'rovdagi tartibda, har bir tanlangan hudud bo'yicha ko'rsatkichlar.")
        List<AnalyticReportRegionBreakdown> regions
) {
}
