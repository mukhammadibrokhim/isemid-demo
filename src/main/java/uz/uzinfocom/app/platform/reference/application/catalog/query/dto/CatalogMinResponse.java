package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

public record CatalogMinResponse(
        Long id,
        String code,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa
) {
}
