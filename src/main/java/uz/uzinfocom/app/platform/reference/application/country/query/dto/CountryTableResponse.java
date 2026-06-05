package uz.uzinfocom.app.platform.reference.application.country.query.dto;

public record CountryTableResponse(
        Long id,
        String code,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa,
        Boolean deleted,
        Integer sortOrder
) {
}
