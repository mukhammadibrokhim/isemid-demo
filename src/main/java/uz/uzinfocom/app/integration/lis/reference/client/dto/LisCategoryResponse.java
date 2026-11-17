package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One row of LIS's {@code categories} catalog — resolves
 * {@code ResearchItemTypeInfo.categoryId} on an act's sample items, each
 * category belonging to one {@link #researchTypeId()}.
 */
public record LisCategoryResponse(
        Long id,
        String nameUz,
        String nameRu,
        Long researchTypeId,
        String researchTypeNameUz,
        String researchTypeNameRu
) {
}
