package uz.uzinfocom.app.platform.iam.application.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * name is the organization's always-present default display name; the
 * locale-specific fields are optional and only populated once the FHIR
 * sync provides them. Callers should fall back to name whenever none of
 * the locale-specific fields resolve to a usable value.
 */
@Schema(description = "Локализованное наименование организации.")
public record OrganizationLocalizedName(
        @Schema(description = "Наименование организации по умолчанию (всегда присутствует).")
        String name,

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
