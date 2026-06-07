package uz.uzinfocom.app.platform.reference.application.lookup.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Localized reference lookup item used by Reference lookup services.")
public record ReferenceItem(
        @Schema(description = "Reference item code.", example = "UZB")
        String code,
        @Schema(description = "Optional parent reference item code.", example = "UZ-AN")
        String parentCode,
        @Schema(description = "Reference item name in Uzbek Latin.", example = "Oʻzbekiston")
        String nameUz,
        @Schema(description = "Reference item name in Uzbek Cyrillic.", example = "Ўзбекистон")
        String nameUzCyril,
        @Schema(description = "Reference item name in Russian.", example = "Узбекистан")
        String nameRu,
        @Schema(description = "Reference item name in Karakalpak.", example = "Ózbekstan")
        String nameKaa
) {
}
