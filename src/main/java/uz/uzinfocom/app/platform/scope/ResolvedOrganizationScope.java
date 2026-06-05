package uz.uzinfocom.app.platform.scope;

import java.util.UUID;

public record ResolvedOrganizationScope(
        OrganizationScopeMode mode,
        UUID organizationUuid,
        String regionCode,
        String districtCode
) {
}
