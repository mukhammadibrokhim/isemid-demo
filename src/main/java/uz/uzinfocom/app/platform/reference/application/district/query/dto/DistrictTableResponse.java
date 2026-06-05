package uz.uzinfocom.app.platform.reference.application.district.query.dto;

public record DistrictTableResponse(
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
