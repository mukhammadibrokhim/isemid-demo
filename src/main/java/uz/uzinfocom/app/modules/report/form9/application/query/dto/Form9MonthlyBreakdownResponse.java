package uz.uzinfocom.app.modules.report.form9.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Месячная разбивка «Form 9» для одного узла (регион/район/организация либо "
        + "вся область доступа вызывающего) и его полного поддерева, за выбранный период "
        + "в сравнении с тем же периодом год назад.")
public record Form9MonthlyBreakdownResponse(
        @Schema(description = "Код узла, для которого построена разбивка.")
        String nodeCode,

        @Schema(description = "Локализованное наименование узла (заголовок таблицы).")
        String nodeName,

        @Schema(description = "13 строк: 12 месяцев (январь→декабрь), затем \"Jami\" (итого).")
        List<Form9MonthRowResponse> rows
) {
}
