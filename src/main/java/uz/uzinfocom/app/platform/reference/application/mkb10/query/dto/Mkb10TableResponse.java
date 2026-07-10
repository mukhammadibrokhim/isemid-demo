package uz.uzinfocom.app.platform.reference.application.mkb10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Строка классификатора МКБ-10 для постраничного табличного ответа.")
public record Mkb10TableResponse(
        @Schema(description = "Внешний идентификатор классификатора.", example = "1500")
        Long id,
        @Schema(description = "Внешний идентификатор родительского узла. Null для узлов верхнего уровня (глав).", example = "12")
        Long parentId,
        @Schema(description = "Уникальный код МКБ-10.", example = "A15")
        String code,
        @Schema(description = "Глубина данного узла в иерархии классификатора, начиная с 0.", example = "3")
        int level,
        @Schema(description = "Признак того, что узел является конечным (назначаемым кодом диагноза).", example = "true")
        boolean lastLevel,
        @Schema(description = "Наименование диагноза на узбекском языке (латиница).", example = "Tuberkulyoz")
        String nameUz,
        @Schema(description = "Наименование диагноза на узбекском языке (кириллица).", example = "Туберкулёз")
        String nameUzCyril,
        @Schema(description = "Наименование диагноза на русском языке.", example = "Туберкулёз")
        String nameRu,
        @Schema(description = "Наименование диагноза на каракалпакском языке.", example = "Túberkulez")
        String nameKaa,
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
