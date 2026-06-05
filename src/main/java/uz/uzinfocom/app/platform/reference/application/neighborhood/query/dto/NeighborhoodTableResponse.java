package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

public record NeighborhoodTableResponse(
        Long id,
        String code,
        String parentCode,
        Integer soatoId,
        Integer parentSoatoId,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa,
        Boolean deleted,
        Integer sortOrder
) {
}
