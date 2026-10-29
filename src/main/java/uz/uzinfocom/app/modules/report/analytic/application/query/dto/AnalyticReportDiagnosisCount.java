package uz.uzinfocom.app.modules.report.analytic.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bitta hudud ichida, bitta tashxis bo'yicha hisoblangan ko'rsatkichlar.")
public record AnalyticReportDiagnosisCount(
        @Schema(description = "KXK-10 kodi.", example = "A09")
        String code,

        @Schema(description = "Tashxis nomi (chaqiruvchining lokaliga mos).",
                example = "Infeksiya bilan bog'liq, deb taxmin qilingan diareya va gastroenterit")
        String name,

        @Schema(description = "Davr ichida tasdiqlangan (status = APPROVED, final_icd10_code) holatlar soni.", example = "0")
        long confirmedCount,

        @Schema(description = "Nisbiy ko'rsatkich: confirmedCount / population * koef. Aholi soni 0 bo'lsa — 0.", example = "0.0")
        double rate
) {
}
