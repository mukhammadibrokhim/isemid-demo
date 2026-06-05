package uz.uzinfocom.app.platform.security.auth;

import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

import java.util.UUID;

public record CachedSecurityOrganization(
        Long id,
        UUID uuid,
        String name,
        Boolean active,
        OrganizationLevel levelType,
        MedicalType medicalType,
        String regionCode,
        String districtCode
) {

    public Organization toDetachedOrganization() {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setUuid(uuid);
        organization.setName(name);
        organization.setActive(active);
        organization.setLevelType(levelType);
        organization.setMedicalType(medicalType);
        organization.setRegionCode(regionCode);
        organization.setDistrictCode(districtCode);
        return organization;
    }
}
