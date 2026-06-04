package uz.uzinfocom.app.platform.reference.application.district.query.dto;

public record DistrictResponse(
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
