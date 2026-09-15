package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * One row of LIS's own organization/department catalog ({@code sesorgs},
 * {@code departments/{orgId}}) — field names and shape are LIS's, verified
 * against its live test response rather than assumed.
 */
public record LisOrganizationResponse(
        Long id,
        String name,
        String nameOrgRu,
        String address,
        Long organizationTypeId,
        String organizationTypeNameUz,
        String organizationTypeNameRu,
        Long regionId,
        String regionName,
        Long districtId,
        String districtName,
        Long parentId,
        String status
) {
}
