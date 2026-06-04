package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

public record NeighborhoodResponse(
        Long id,
        String code,
        String parentCode,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa,
        Boolean deleted,
        Integer sortOrder
) {
}
