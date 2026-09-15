package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One row of LIS's {@code research-types} catalog — the research family
 * (WATER/FOOD/SOIL and their subtypes) an act's {@code purpose}/sample items
 * are classified under. Not to be confused with {@code LisResearchCode}
 * (this codebase's own WATER/FOOD/SOIL enum used to resolve the LIS
 * act-template id) — that's a fixed 3-value mapping per act type, this is
 * LIS's much larger, free-standing lookup catalog.
 */
public record LisResearchTypeResponse(
        Long id,
        String nameUz,
        String nameRu,
        String description,
        String researchCode
) {
}
