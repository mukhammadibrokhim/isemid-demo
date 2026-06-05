package uz.uzinfocom.app.platform.reference.application.lookup.dto;

public record ReferenceItem(
        String code,
        String parentCode,
        String nameUz,
        String nameUzCyril,
        String nameRu,
        String nameKaa
) {
}
