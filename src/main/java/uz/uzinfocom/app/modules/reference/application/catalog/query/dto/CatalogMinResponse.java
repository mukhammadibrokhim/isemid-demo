package uz.uzinfocom.app.modules.reference.application.catalog.query.dto;

public record CatalogMinResponse(
        Long id,
        String code,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa
) {
}
