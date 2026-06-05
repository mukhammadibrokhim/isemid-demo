package uz.uzinfocom.app.platform.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

@Component
@RequiredArgsConstructor
public class OrganizationScopeResolver {

    private final OrganizationScopeProperties properties;

    public ResolvedOrganizationScope resolve(Organization organization) {
        if (organization == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        if (organization.getMedicalType() != MedicalType.SANEPID_SERVICE) {
            return ownOrganization(organization);
        }

        OrganizationLevel level = organization.getLevelType();
        if (level == null) {
            return ownOrganization(organization);
        }

        return switch (level) {
            case REPUBLICAN -> new ResolvedOrganizationScope(OrganizationScopeMode.ALL, organization.getUuid(), null, null);
            case REGIONAL -> regionScope(organization);
            case URBAN -> isTashkentUrban(organization) ? regionScope(organization) : districtScope(organization);
            case DISTRICT, AREA, INTERDISTRICT -> districtScope(organization);
            case NOT_DEFINED -> ownOrganization(organization);
        };
    }

    private ResolvedOrganizationScope ownOrganization(Organization organization) {
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.ORGANIZATION,
                organization.getUuid(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }

    private ResolvedOrganizationScope regionScope(Organization organization) {
        if (!StringUtils.hasText(organization.getRegionCode())) {
            throw new ScopeViolationException("organization.scope_violation");
        }
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.REGION,
                organization.getUuid(),
                organization.getRegionCode(),
                null
        );
    }

    private ResolvedOrganizationScope districtScope(Organization organization) {
        if (!StringUtils.hasText(organization.getDistrictCode())) {
            return ownOrganization(organization);
        }
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.DISTRICT,
                organization.getUuid(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }

    private boolean isTashkentUrban(Organization organization) {
        return StringUtils.hasText(organization.getRegionCode())
                && organization.getRegionCode().equalsIgnoreCase(properties.getTashkentRegionCode());
    }
}
