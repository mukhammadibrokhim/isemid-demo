package uz.uzinfocom.app.platform.reference.application.region.query.dto;

public record RegionTableResponse(
        Long id,
        String code,
        String parentCode,
        Integer soatoId,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa,
        Boolean deleted,
        Integer sortOrder
) {
}
