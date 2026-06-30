package uz.uzinfocom.app.platform.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

@Component
@RequiredArgsConstructor
public class OrganizationScopeResolver {

    private final OrganizationScopeProperties properties;

    public ResolvedOrganizationScope resolve(Organization organization) {
        if (organization == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        MedicalType medicalType = organization.getMedicalType();
        OrganizationLevel level = organization.getLevelType();

        /*
         * Muhim qoida:
         * Level-based scope faqat SANEPID_SERVICE uchun ishlaydi.
         * Qolgan MEDICAL / EDUCATIONAL / OTHER organizationlar faqat o‘z organization_id bo‘yicha ishlaydi.
         */
        if (medicalType != MedicalType.SANEPID_SERVICE) {
            return ownOrganization(organization);
        }

        if (level == null) {
            return ownOrganization(organization);
        }

        return switch (level) {
            case REPUBLICAN -> allScope(organization);
            case REGIONAL -> regionScope(organization);
            case URBAN -> isTashkentUrban(organization)
                    ? regionScope(organization)
                    : districtScope(organization);
            case DISTRICT, AREA, INTERDISTRICT -> districtScope(organization);
            case NOT_DEFINED -> ownOrganization(organization);
        };
    }

    private ResolvedOrganizationScope allScope(Organization organization) {
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.ALL,
                organization.getId(),
                organization.getUuid(),
                organization.getMedicalType(),
                organization.getLevelType(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }

    private ResolvedOrganizationScope ownOrganization(Organization organization) {
        return new ResolvedOrganizationScope(
                OrganizationScopeMode.ORGANIZATION,
                organization.getId(),
                organization.getUuid(),
                organization.getMedicalType(),
                organization.getLevelType(),
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
                organization.getId(),
                organization.getUuid(),
                organization.getMedicalType(),
                organization.getLevelType(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }

    private ResolvedOrganizationScope districtScope(Organization organization) {
        if (!StringUtils.hasText(organization.getDistrictCode())) {
            return ownOrganization(organization);
        }

        return new ResolvedOrganizationScope(
                OrganizationScopeMode.DISTRICT,
                organization.getId(),
                organization.getUuid(),
                organization.getMedicalType(),
                organization.getLevelType(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }

    private boolean isTashkentUrban(Organization organization) {
        return StringUtils.hasText(organization.getRegionCode())
                && organization.getRegionCode().equalsIgnoreCase(properties.getTashkentRegionCode());
    }
}