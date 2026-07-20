package uz.uzinfocom.app.platform.dashboard.application.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.MedicalTypeCountProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLevelCountProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.RoleUserCountProjection;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.UserStatsRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: verifies scope-mode branching (medical institutions
 * count/breakdown) and delegation to {@link UserStatsRepository} for the
 * user/role breakdown. This dashboard is deliberately lightweight now — no
 * case/card/act statistics live here anymore (see {@code
 * Form058DashboardQueryServiceTest} etc. for those, behind their own
 * {@code /home/{module}} endpoints).
 */
class HomeDashboardQueryServiceTest {

    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver =
            mock(OrganizationScopeOrganizationIdResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final UserStatsRepository userStatsRepository = mock(UserStatsRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);

    private final HomeDashboardQueryService service = new HomeDashboardQueryService(
            organizationScopeResolver,
            organizationScopeOrganizationIdResolver,
            organizationRepository,
            userStatsRepository,
            referenceLookupService,
            Runnable::run
    );

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void throwsScopeViolationWhenNoOrganizationSelected() {
        assertThatThrownBy(service::getHome).isInstanceOf(ScopeViolationException.class);
    }

    @Test
    void responseCarriesAGeneratedAtTimestampCloseToNow() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ORGANIZATION, null, null));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.generatedAt()).isNotNull();
        assertThat(response.generatedAt()).isCloseTo(Instant.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    void organizationScopeReportsSingleInstitutionAndOwnMedicalType() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutions().total()).isEqualTo(1L);
        assertThat(response.medicalInstitutions().byMedicalType()).hasSize(1);
        assertThat(response.medicalInstitutions().byMedicalType().getFirst().count()).isEqualTo(1L);
        assertThat(response.medicalInstitutions().byLevelType()).hasSize(1);
        verify(organizationRepository, never()).countActiveByMedicalType(any(), any());
        verify(organizationRepository, never()).countActiveByLevelType(any(), any());
    }

    @Test
    void allScopeUsesNationwideInstitutionCount() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.ALL, null, null));
        when(organizationRepository.countByActiveTrue()).thenReturn(1245L);

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutions().total()).isEqualTo(1245L);
        verify(organizationScopeOrganizationIdResolver, never()).resolveScopeOrganizationIds(any(), any(), any());
    }

    @Test
    void regionScopeInstitutionCountComesFromResolvedIdListSize() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.REGION, "REGION1", null));
        when(organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                OrganizationScopeMode.REGION, "REGION1", null
        )).thenReturn(List.of(1L, 2L, 3L));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutions().total()).isEqualTo(3L);
    }

    @Test
    void medicalInstitutionsBreakdownMapsProjectionsForRegionScope() {
        CurrentOrganizationContext.set(organization());
        when(organizationScopeResolver.resolve(any()))
                .thenReturn(scopeOf(OrganizationScopeMode.REGION, "REGION1", null));

        when(organizationRepository.countActiveByMedicalType("REGION1", null)).thenReturn(List.of(
                new MedicalTypeCountProjection(MedicalType.SANEPID_SERVICE, 12L)
        ));
        when(organizationRepository.countActiveByLevelType("REGION1", null)).thenReturn(List.of(
                new OrganizationLevelCountProjection(OrganizationLevel.DISTRICT, 9L),
                new OrganizationLevelCountProjection(OrganizationLevel.REGIONAL, 3L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.medicalInstitutions().byMedicalType()).hasSize(1);
        assertThat(response.medicalInstitutions().byMedicalType().getFirst().medicalType()).isEqualTo(MedicalType.SANEPID_SERVICE);
        assertThat(response.medicalInstitutions().byMedicalType().getFirst().count()).isEqualTo(12L);

        assertThat(response.medicalInstitutions().byLevelType()).hasSize(2);
        assertThat(response.medicalInstitutions().byLevelType())
                .anySatisfy(item -> {
                    assertThat(item.levelType()).isEqualTo(OrganizationLevel.DISTRICT);
                    assertThat(item.count()).isEqualTo(9L);
                });
    }

    @Test
    void usersReflectTotalAndRoleBreakdownFromUserStatsRepository() {
        CurrentOrganizationContext.set(organization());
        ResolvedOrganizationScope scope = scopeOf(OrganizationScopeMode.ORGANIZATION, null, null);
        when(organizationScopeResolver.resolve(any())).thenReturn(scope);

        when(userStatsRepository.countTotal(scope)).thenReturn(42L);
        when(userStatsRepository.countByRole(scope)).thenReturn(List.of(
                new RoleUserCountProjection("ADMIN", 2L),
                new RoleUserCountProjection("OPERATOR", 40L)
        ));

        HomeDashboardResponse response = service.getHome();

        assertThat(response.users().total()).isEqualTo(42L);
        assertThat(response.users().byRole()).hasSize(2);
        assertThat(response.users().byRole())
                .anySatisfy(item -> {
                    assertThat(item.role()).isEqualTo("OPERATOR");
                    assertThat(item.count()).isEqualTo(40L);
                });
    }

    private ResolvedOrganizationScope scopeOf(OrganizationScopeMode mode, String regionCode, String districtCode) {
        return new ResolvedOrganizationScope(mode, 1L, null, null, null, regionCode, districtCode);
    }

    private Organization organization() {
        Organization organization = new Organization();
        organization.setId(1L);
        return organization;
    }
}
