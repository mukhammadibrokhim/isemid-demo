package uz.uzinfocom.app.platform.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.exception.ScopeViolationException;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

@Component
@RequiredArgsConstructor
public class OrganizationScopeResolver {

    private final OrganizationScopeProperties properties;

    public ResolvedOrganizationScope resolve(Organization organization) {
        if (organization == null) {
            throw new ScopeViolationException("error.scope_violation");
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
            case REGIONAL -> stateScope(organization);
            case URBAN -> isTashkentUrban(organization) ? stateScope(organization) : cityScope(organization);
            case DISTRICT, AREA, INTERDISTRICT -> cityScope(organization);
            case NOT_DEFINED -> ownOrganization(organization);
        };
    }

    private ResolvedOrganizationScope ownOrganization(Organization organization) {
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.ORGANIZATION,
                organization.getUuid(),
                organization.getStateCode(),
                organization.getCityCode()
        );
    }

    private ResolvedOrganizationScope stateScope(Organization organization) {
        if (!StringUtils.hasText(organization.getStateCode())) {
            throw new ScopeViolationException("error.scope_violation");
        }
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.STATE,
                organization.getUuid(),
                organization.getStateCode(),
                null
        );
    }

    private ResolvedOrganizationScope cityScope(Organization organization) {
        if (!StringUtils.hasText(organization.getCityCode())) {
            return ownOrganization(organization);
        }
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.CITY,
                organization.getUuid(),
                organization.getStateCode(),
                organization.getCityCode()
        );
    }

    private boolean isTashkentUrban(Organization organization) {
        return StringUtils.hasText(organization.getStateCode())
                && organization.getStateCode().equalsIgnoreCase(properties.getTashkentStateCode());
    }
}
