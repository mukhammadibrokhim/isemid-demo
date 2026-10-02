package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One row of LIS's {@code item-types} catalog — resolves
 * {@code ResearchItemTypeInfo.itemTypeId} on an act's sample items, each item
 * type belonging to one {@link #itemCategoryId()} within one
 * {@link #researchTypeId()}.
 */
public record LisItemTypeResponse(
        Long id,
        String nameUz,
        String nameRu,
        Long itemCategoryId,
        String itemCategoryNameUz,
        String itemCategoryNameRu,
        Long researchTypeId,
        String researchTypeNameUz,
        String researchTypeNameRu
) {
}
