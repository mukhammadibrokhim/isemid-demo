package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = SecurityUserCacheServiceTest.TestConfig.class)
class SecurityUserCacheServiceTest {

    @Autowired
    private SecurityUserCacheService securityUserCacheService;

    @Autowired
    private SelectedOrganizationSecurityCacheService selectedOrganizationSecurityCacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager securityCacheManager;

    @BeforeEach
    void setUp() {
        securityCacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).clear();
        securityCacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID).clear();
        clearInvocations(userRepository);
    }

    @Test
    void firstCallHitsDbAndSecondCallUsesCachedSecurityUser() {
        UUID organizationUuid = UUID.randomUUID();
        when(userRepository.findSecurityUserWithOrganizationsById(1L))
                .thenReturn(Optional.of(user(1L, organization(20L, organizationUuid))));

        Optional<CachedSecurityUser> first = securityUserCacheService.loadByUserId(1L);
        Optional<CachedSecurityUser> second = securityUserCacheService.loadByUserId(1L);

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(second.orElseThrow().organizations())
                .extracting(CachedSecurityOrganization::uuid)
                .containsExactly(organizationUuid);

        verify(userRepository, times(1)).findSecurityUserWithOrganizationsById(1L);
    }

    @Test
    void selectedOrganizationIsResolvedFromLoadedUserOrganizationsAndThenCached() {
        UUID organizationUuid = UUID.randomUUID();
        when(userRepository.findSecurityUserWithOrganizationsById(1L))
                .thenReturn(Optional.of(user(1L, organization(20L, organizationUuid))));

        Optional<CachedSecurityOrganization> first =
                selectedOrganizationSecurityCacheService.resolveSelectedOrganization(1L, organizationUuid);
        Optional<CachedSecurityOrganization> second =
                selectedOrganizationSecurityCacheService.resolveSelectedOrganization(1L, organizationUuid);

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(second.orElseThrow().id()).isEqualTo(20L);

        verify(userRepository, times(1)).findSecurityUserWithOrganizationsById(1L);
    }

    @Test
    void invalidSelectedOrganizationReturnsEmptyWithoutSeparateOrganizationQuery() {
        UUID organizationUuid = UUID.randomUUID();
        when(userRepository.findSecurityUserWithOrganizationsById(1L))
                .thenReturn(Optional.of(user(1L, organization(20L, organizationUuid))));

        Optional<CachedSecurityOrganization> selected =
                selectedOrganizationSecurityCacheService.resolveSelectedOrganization(1L, UUID.randomUUID());

        assertThat(selected).isEmpty();
        verify(userRepository, times(1)).findSecurityUserWithOrganizationsById(1L);
    }

    private User user(Long id, Organization organization) {
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .username("doctor")
                .nnuzb("123456789")
                .active(true)
                .organizations(Set.of(organization))
                .build();
        user.setId(id);
        return user;
    }

    private Organization organization(Long id, UUID uuid) {
        Organization organization = Organization.builder()
                .uuid(uuid)
                .name("Central Clinic")
                .active(true)
                .levelType(OrganizationLevel.REPUBLICAN)
                .medicalType(MedicalType.SANEPID_SERVICE)
                .stateCode("17")
                .cityCode("1701")
                .build();
        organization.setId(id);
        return organization;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean(name = "securityCacheManager")
        CacheManager securityCacheManager() {
            return new ConcurrentMapCacheManager(
                    SecurityCacheNames.SECURITY_USER_BY_ID,
                    SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID
            );
        }

        @Bean
        SecurityUserCacheService securityUserCacheService(UserRepository userRepository) {
            return new SecurityUserCacheService(userRepository);
        }

        @Bean
        SelectedOrganizationSecurityCacheService selectedOrganizationSecurityCacheService(
                SecurityUserCacheService securityUserCacheService
        ) {
            return new SelectedOrganizationSecurityCacheService(securityUserCacheService);
        }
    }
}
