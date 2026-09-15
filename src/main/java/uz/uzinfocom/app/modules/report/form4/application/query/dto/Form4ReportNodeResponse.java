package uz.uzinfocom.app.modules.report.form4.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bitta tugun (region/tuman/tashkilot) — «Form 4: Kasallanishning ijtimoiy "
        + "tarkibi» hisoboti daraxti.")
public record Form4ReportNodeResponse(
        @Schema(description = "Tugun kodi: region/tuman kodi yoki tashkilot id'si.")
        String code,

        @Schema(description = "Tugunning lokalizatsiya qilingan nomi.")
        String name,

        @Schema(description = "Tugunning chuqurroq darajasi bor-yo'qligi (kengaytirish strelkasi uchun).")
        boolean hasChildren,

        @Schema(description = "Tasdiqlangan holatlar bo'yicha (status = APPROVED) toifalar kesimi.")
        Form4CategoryCountBlockResponse confirmed,

        @Schema(description = "Birlamchi holatlar bo'yicha (status NOT IN (APPROVED, CANCELED)) toifalar kesimi.")
        Form4CategoryCountBlockResponse primary
) {
}
