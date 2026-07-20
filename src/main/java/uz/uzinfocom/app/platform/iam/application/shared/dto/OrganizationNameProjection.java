package uz.uzinfocom.app.platform.iam.application.shared.dto;

/**
 * Shared read projection — an organization's id paired with its raw
 * locale-name fields (not yet resolved to a single display name), for any
 * caller that needs both the id (e.g. to join against a count-by-org-id
 * map) and the name (via {@code OrganizationNameResolver}).
 */
public record OrganizationNameProjection(
        Long id,
        String name,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa
) {
}
