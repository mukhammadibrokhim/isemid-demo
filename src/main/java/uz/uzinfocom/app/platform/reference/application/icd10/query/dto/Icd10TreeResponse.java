package uz.uzinfocom.app.platform.reference.application.icd10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Узел дерева МКБ-10 для навигации по справочнику: наименование уже разрешено под локаль запроса.")
public record Icd10TreeResponse(

        @Schema(description = "Внешний идентификатор классификатора, присвоенный исходным набором данных МКБ-10 ВОЗ.", example = "1500")
        Long id,

        @Schema(description = "Устаревший/альтернативный числовой идентификатор из предыдущей системы классификации.",
                example = "1500")
        Long secondaryId,

        @Schema(description = "Внешний идентификатор родительского узла. Null для узлов верхнего уровня (глав).", example = "12")
        Long parentId,

        @Schema(description = "Уникальный код МКБ-10.", example = "A15")
        String code,

        @Schema(description = "Глубина данного узла в иерархии классификатора, начиная с 0.", example = "3")
        int level,

        @Schema(description = "Признак того, что узел является конечным (назначаемым кодом диагноза), а не заголовком категории.",
                example = "true")
        boolean lastLevel,

        @Schema(description = "Наименование диагноза, разрешённое под локаль текущего запроса.", example = "Tuberkulyoz")
        String name,

        @Schema(description = "Произвольное примечание к данному узлу классификатора.")
        String comment,

        @Schema(description = "Сколько раз может быть использован/выбран данный код.", example = "1")
        Integer usageLimit,

        @Schema(description = "Количество непосредственных дочерних узлов данного узла.", example = "5")
        long childrenCount,

        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
