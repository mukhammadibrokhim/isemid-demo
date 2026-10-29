package uz.uzinfocom.app.modules.report.form8.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Социальный состав «Form 8» для одного узла (регион/район/организация либо вся "
        + "область доступа вызывающего) и его полного поддерева, за выбранный период "
        + "в сравнении с тем же периодом год назад.")
public record Form8CategoryBreakdownResponse(
        @Schema(description = "Код узла, для которого построена разбивка.")
        String nodeCode,

        @Schema(description = "Локализованное наименование узла (заголовок таблицы).")
        String nodeName,

        @Schema(description = "Строки по социальным категориям, первой строкой — \"Jami\" (итого).")
        List<Form8CategoryRowResponse> rows
) {
}
