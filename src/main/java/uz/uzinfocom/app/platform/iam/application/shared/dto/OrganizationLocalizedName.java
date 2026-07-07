package uz.uzinfocom.app.platform.iam.application.shared.dto;

/**
 * name is the organization's always-present default display name; the
 * locale-specific fields are optional and only populated once the FHIR
 * sync provides them. Callers should fall back to name whenever none of
 * the locale-specific fields resolve to a usable value.
 */
public record OrganizationLocalizedName(
        String name,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa
) {
}
