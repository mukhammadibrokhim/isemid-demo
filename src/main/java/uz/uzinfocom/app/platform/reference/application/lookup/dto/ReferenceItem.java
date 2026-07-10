package uz.uzinfocom.app.platform.reference.application.lookup.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Локализованный элемент справочника, используемый сервисами поиска по справочникам.")
public record ReferenceItem(
        @Schema(description = "Код элемента справочника.", example = "UZB")
        String code,
        @Schema(description = "Необязательный код родительского элемента справочника.", example = "UZ-AN")
        String parentCode,
        @Schema(description = "Наименование элемента справочника на узбекском языке (латиница).", example = "Oʻzbekiston")
        String nameUz,
        @Schema(description = "Наименование элемента справочника на узбекском языке (кириллица).", example = "Ўзбекистон")
        String nameUzCyril,
        @Schema(description = "Наименование элемента справочника на русском языке.", example = "Узбекистан")
        String nameRu,
        @Schema(description = "Наименование элемента справочника на каракалпакском языке.", example = "Ózbekstan")
        String nameKaa
) {
}
