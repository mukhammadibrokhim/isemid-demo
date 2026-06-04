package uz.uzinfocom.app.platform.reference.dto;

public record RegionResponse(
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
