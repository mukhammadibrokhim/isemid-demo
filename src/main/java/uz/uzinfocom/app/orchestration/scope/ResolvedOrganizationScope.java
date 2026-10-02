package uz.uzinfocom.app.orchestration.scope;

import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;

import java.util.UUID;

public record ResolvedOrganizationScope(
        OrganizationScopeMode mode,
        Long organizationId,
        UUID organizationUuid,
        MedicalType medicalType,
        OrganizationLevel levelType,
        String regionCode,
        String districtCode
) {
    public boolean isSanepidService() {
        return medicalType == MedicalType.SANEPID_SERVICE;
    }
}