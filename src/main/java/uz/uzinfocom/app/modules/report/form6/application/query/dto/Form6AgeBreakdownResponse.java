package uz.uzinfocom.app.modules.report.form6.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Возрастная структура «Form 6» для одного узла (регион/район/организация "
        + "либо вся область доступа вызывающего) и его полного поддерева, за выбранный период.")
public record Form6AgeBreakdownResponse(
        @Schema(description = "Код узла, для которого построена разбивка.")
        String nodeCode,

        @Schema(description = "Локализованное наименование узла (заголовок таблицы).")
        String nodeName,

        @Schema(description = "Строки по возрастным группам, первой строкой — \"Jami\" (итого).")
        List<Form6AgeGroupRowResponse> rows
) {
}
