package uz.uzinfocom.app.orchestration.scope;

import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;

import java.util.EnumSet;
import java.util.Set;
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
    /**
     * Medical types whose visibility follows their {@link OrganizationLevel}
     * (republic -> everything, region -> that region, district -> that district).
     * Every other type only ever sees its own organization.
     */
    private static final Set<MedicalType> LEVEL_SCOPED_TYPES =
            EnumSet.of(MedicalType.SANEPID_SERVICE, MedicalType.HEALTH_DEPARTMENT);

    public static boolean hasLevelScope(MedicalType medicalType) {
        return medicalType != null && LEVEL_SCOPED_TYPES.contains(medicalType);
    }

    public boolean hasLevelScope() {
        return hasLevelScope(medicalType);
    }
}