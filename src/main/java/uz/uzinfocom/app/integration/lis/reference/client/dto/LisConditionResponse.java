package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One entry of LIS's {@code reference-dictionaries?type=CONDITIONS} catalog
 * — storage/delivery/special conditions selectable on an act.
 */
public record LisConditionResponse(
        Long id,
        String nameUz,
        String nameRu
) {
}
