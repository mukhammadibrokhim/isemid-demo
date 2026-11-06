package uz.uzinfocom.app.modules.report.form282.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Одна строка отчёта «Form 28.2 (по территориям)» — один узел географии (республика / "
        + "регион / район / организация) либо итоговая строка (\"Jami\"). Столбцы строки — болезни: по одной "
        + "ячейке diseases[] на каждую запись справочника ручных отчётов с тегом FORM_28_2, в одном и том же "
        + "порядке для всех строк ответа.")
public record Form282ByTerritoryNodeResponse(
        @Schema(description = "Код узла: код региона/района, id организации, \"UZ\" для республики или "
                + "\"TOTAL\" для итоговой строки.")
        String code,

        @Schema(description = "Локализованное наименование узла географии.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "По одной ячейке на каждую болезнь (запись справочника FORM_28_2), в стабильном "
                + "порядке по коду записи.")
        List<Form282DiseaseCellResponse> diseases
) {
}
