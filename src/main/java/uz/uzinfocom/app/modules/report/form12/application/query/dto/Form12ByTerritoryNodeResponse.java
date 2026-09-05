package uz.uzinfocom.app.modules.report.form12.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Одна строка отчёта «Form 12 (по территориям)» — один узел географии (республика / "
        + "регион / район / организация) либо итоговая строка (\"Jami\"). Столбцы строки — нозологические "
        + "формы: по одной ячейке diseases[] на каждую запись справочника ручных отчётов с тегом FORM_12, в "
        + "одном и том же порядке для всех строк ответа.")
public record Form12ByTerritoryNodeResponse(
        @Schema(description = "Код узла: код региона/района, id организации, \"UZ\" для республики или "
                + "\"TOTAL\" для итоговой строки.")
        String code,

        @Schema(description = "Локализованное наименование узла географии.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "По одной ячейке на каждую нозологическую форму (запись справочника FORM_12), в "
                + "стабильном порядке по коду записи.")
        List<Form12DiseaseCellResponse> diseases
) {
}
