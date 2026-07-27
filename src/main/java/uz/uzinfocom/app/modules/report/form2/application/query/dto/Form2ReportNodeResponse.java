package uz.uzinfocom.app.modules.report.form2.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 2: социальный состав "
        + "заболеваемости».")
public record Form2ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Всего первичных случаев за период (включая категории без отдельной колонки).")
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
        long unemployed
) {
}
