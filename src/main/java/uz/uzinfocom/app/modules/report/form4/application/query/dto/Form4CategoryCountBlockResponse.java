package uz.uzinfocom.app.modules.report.form4.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bitta metrika (confirmed yoki primary) bo'yicha bemorlar toifasiga ko'ra "
        + "taqsimlangan sonlar.")
public record Form4CategoryCountBlockResponse(
        @Schema(description = "Jami (toifasi alohida ustunda ko'rsatilmagan toifalarni ham qo'shib).")
        long total,

        @Schema(description = "Maktabgacha tarbiya yoshidagi uyushmagan bolalar (category_code = NO_ORGANIZED).")
        long unorganizedPreschool,

        @Schema(description = "Maktabgacha tarbiya yoshidagi uyushgan bolalar (category_code = ORGANIZED).")
        long organizedPreschool,

        @Schema(description = "Maktab o'quvchilari (category_code = STUDENT_SCHOOL).")
        long schoolStudents,

        @Schema(description = "O'rta maxsus ta'lim muassasalari talabalari (category_code = MIDDLE_STUDENT).")
        long vocationalStudents,

        @Schema(description = "OTM talabalari (category_code = STUDENT).")
        long universityStudents,

        @Schema(description = "Xizmatchilar (category_code = SEREVANTS).")
        long employees,

        @Schema(description = "Ishchilar (category_code = WORKER).")
        long workers,

        @Schema(description = "Tibbiyot xodimlari (category_code = MEDICAL_WORKER).")
        long medicalStaff,

        @Schema(description = "Ishlamaydiganlar (category_code = NOT_EMPLOYED).")
        long unemployed,

        @Schema(description = "Nafaqaxo'rlar (category_code = PENSIONER).")
        long pensioners,

        @Schema(description = "Muayyan yashash joyga ega bo'lmaganlar (category_code = UNSHELTRED).")
        long unsheltered
) {
}
