package uz.uzinfocom.app.orchestration.scope;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationScopeResolverTest {

    private final OrganizationScopeResolver resolver = new OrganizationScopeResolver(new OrganizationScopeProperties());

    private static Organization org(MedicalType type, OrganizationLevel level, String region, String district) {
        Organization organization = new Organization();
        organization.setId(77L);
        organization.setMedicalType(type);
        organization.setLevelType(level);
        organization.setRegionCode(region);
        organization.setDistrictCode(district);
        return organization;
    }

    @Test
    void healthDepartmentOnRepublicLevelSeesEverything() {
        ResolvedOrganizationScope scope =
                resolver.resolve(org(MedicalType.HEALTH_DEPARTMENT, OrganizationLevel.REPUBLICAN, "UZ-TK", "TK-277"));

        assertThat(scope.mode()).isEqualTo(OrganizationScopeMode.ALL);
        assertThat(scope.hasLevelScope()).isTrue();
    }

    @Test
    void healthDepartmentOnRegionalLevelIsLimitedToItsRegion() {
        ResolvedOrganizationScope scope =
                resolver.resolve(org(MedicalType.HEALTH_DEPARTMENT, OrganizationLevel.REGIONAL, "UZ-SA", "SA-001"));

        assertThat(scope.mode()).isEqualTo(OrganizationScopeMode.REGION);
    }

    @Test
    void healthDepartmentOnDistrictLevelIsLimitedToItsDistrict() {
        ResolvedOrganizationScope scope =
                resolver.resolve(org(MedicalType.HEALTH_DEPARTMENT, OrganizationLevel.DISTRICT, "UZ-SA", "SA-001"));

        assertThat(scope.mode()).isEqualTo(OrganizationScopeMode.DISTRICT);
    }

    @Test
    void sanepidServiceKeepsLevelBasedScope() {
        ResolvedOrganizationScope scope =
                resolver.resolve(org(MedicalType.SANEPID_SERVICE, OrganizationLevel.REPUBLICAN, "UZ-TK", "TK-277"));

        assertThat(scope.mode()).isEqualTo(OrganizationScopeMode.ALL);
    }

    @Test
    void otherMedicalTypesStayOnOwnOrganizationEvenOnRepublicLevel() {
        ResolvedOrganizationScope scope =
                resolver.resolve(org(MedicalType.HOSPITAL, OrganizationLevel.REPUBLICAN, "UZ-TK", "TK-277"));

        assertThat(scope.mode()).isEqualTo(OrganizationScopeMode.ORGANIZATION);
        assertThat(scope.hasLevelScope()).isFalse();
    }
}
