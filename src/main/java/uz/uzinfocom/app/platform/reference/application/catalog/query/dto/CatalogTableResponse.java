package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Элемент каталога для постраничного табличного ответа.")
public record CatalogTableResponse(

        @Schema(
                description = "Внутренний идентификатор элемента каталога.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Тип каталога.",
                example = "GENDER"
        )
        String type,

        @Schema(
                description = "Уникальный код элемента внутри выбранного типа каталога.",
                example = "MALE"
        )
        String code,

        @Schema(
                description = "Необязательный код родительского элемента внутри того же типа каталога.",
                example = "PERSON",
                nullable = true
        )
        String parentCode,

        @Schema(
                description = "Наименование элемента каталога на узбекском языке (латиница).",
                example = "Erkak"
        )
        String nameUz,

        @Schema(
                description = "Наименование элемента каталога на узбекском языке (кириллица).",
                example = "Эркак"
        )
        String nameUzCyril,

        @Schema(
                description = "Наименование элемента каталога на русском языке.",
                example = "Мужской"
        )
        String nameRu,

        @Schema(
                description = "Наименование элемента каталога на каракалпакском языке.",
                example = "Ер адам"
        )
        String nameKaa
) {
}
