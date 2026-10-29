package uz.uzinfocom.app.modules.iam.application.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The organization's per-language names. Any of them may be blank when the
 * remote system has no translation; OrganizationNameResolver picks the one
 * matching the request locale and falls back to another that has a value.
 */
@Schema(description = "Локализованное наименование организации.")
public record OrganizationLocalizedName(
        @Schema(description = "Наименование на узбекском языке (латиница).")
        String nameUz,

        @Schema(description = "Наименование на узбекском языке (кириллица).")
        String nameUzCyril,

        @Schema(description = "Наименование на русском языке.")
        String nameRu,

        @Schema(description = "Наименование на каракалпакском языке.")
        String nameKaa
) {
}
