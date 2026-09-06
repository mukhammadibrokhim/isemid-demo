package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Bitta tanlangan hudud bo'yicha hisoblangan ko'rsatkichlar.")
public record AnalyticReportRegionBreakdown(
        @Schema(description = "Hudud kodi.", example = "UZ-TK")
        String regionCode,

        @Schema(description = "Hudud nomi.", example = "Toshkent shahri")
        String regionName,

        @Schema(description = "Joriy yil uchun ref_population'dan olingan aholi soni. Ma'lumot topilmasa — 0.", example = "10000")
        long population,

        @Schema(description = "Tanlangan har bir tashxis bo'yicha ko'rsatkichlar, so'rovdagi tartibda.")
        List<AnalyticReportDiagnosisCount> diagnoses
) {
}
