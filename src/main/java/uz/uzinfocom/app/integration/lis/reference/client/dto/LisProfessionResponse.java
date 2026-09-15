package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One row of LIS's {@code professions} catalog — resolves
 * {@code involvedProfessionId}/{@code collectorProfessionId} on an act.
 */
public record LisProfessionResponse(
        Long id,
        String code,
        String nameUz,
        String nameRu,
        String nameUzCyrl
) {
}
