package uz.uzinfocom.app.modules.iam.application.user.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.application.user.query.mapper.UserQueryMapper;
import uz.uzinfocom.app.modules.iam.application.user.query.specification.UserSpecification;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.modules.iam.repository.UserRepository;
import uz.uzinfocom.app.modules.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserQueryServiceAccessTest {

    private static final long TARGET_ID = 7L;
    private static final String FULL_NNUZB = "31234567890123";

    private final UserRepository userRepo = mock(UserRepository.class);
    private final UserQueryMapper mapper = mock(UserQueryMapper.class);
    private final AuditResolver auditResolver = mock(AuditResolver.class);
    private final UserSpecification userSpecification = mock(UserSpecification.class);
    private final OrganizationScopeResolver scopeResolver = mock(OrganizationScopeResolver.class);
    private final AdminAccessGuard adminAccessGuard = mock(AdminAccessGuard.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);

    private final UserQueryService service = new UserQueryService(
            userRepo, mapper, auditResolver, mock(OrganizationNameResolver.class), userSpecification,
            scopeResolver, adminAccessGuard, currentUserProvider
    );

    private final User target = mock(User.class);
    private final ResolvedOrganizationScope scope = new ResolvedOrganizationScope(
            OrganizationScopeMode.ORGANIZATION, 1L, UUID.randomUUID(), null, null, null, null
    );

    @BeforeEach
    void setUp() {
        Organization current = mock(Organization.class);
        CurrentOrganizationContext.set(current);
        when(scopeResolver.resolve(current)).thenReturn(scope);

        when(target.getId()).thenReturn(TARGET_ID);
        when(userRepo.findWithOrganizationsById(TARGET_ID)).thenReturn(Optional.of(target));
        when(mapper.toDetailedResponse(any(), any())).thenReturn(detail(FULL_NNUZB));
        when(userSpecification.visibleById(TARGET_ID, scope)).thenReturn(mock(Specification.class));
    }

    @AfterEach
    void tearDown() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void userOutsideCallerScopeIsReportedAsNotFound() {
        when(userRepo.exists(any(Specification.class))).thenReturn(false);

        assertThatThrownBy(() -> service.getRequiredById(TARGET_ID)).isInstanceOf(NotFoundException.class);
        verify(mapper, never()).toDetailedResponse(any(), any());
    }

    @Test
    void userInsideScopeIsReturnedWithMaskedIdentifier() {
        when(userRepo.exists(any(Specification.class))).thenReturn(true);

        UserDetailedResponse response = service.getRequiredById(TARGET_ID);

        assertThat(response.nnuzb()).isEqualTo("**********0123");
    }

    @Test
    void uuidLookupObeysTheSameScopeRule() {
        UUID uuid = UUID.randomUUID();
        when(userRepo.findByUuid(uuid)).thenReturn(Optional.of(target));
        when(userRepo.exists(any(Specification.class))).thenReturn(false);

        assertThatThrownBy(() -> service.getRequiredByUuid(uuid)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void organizationsOfUserOutsideScopeAreNotListed() {
        when(userRepo.exists(any(Specification.class))).thenReturn(false);

        assertThatThrownBy(() -> service.findOrganizations(TARGET_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void administratorSkipsScopeAndSeesFullIdentifier() {
        when(adminAccessGuard.isAdmin()).thenReturn(true);

        UserDetailedResponse response = service.getRequiredById(TARGET_ID);

        assertThat(response.nnuzb()).isEqualTo(FULL_NNUZB);
        verify(userRepo, never()).exists(any(Specification.class));
    }

    @Test
    void userSeesOwnRecordInFullEvenOutsideScopeRules() {
        when(currentUserProvider.userIdOrNull()).thenReturn(TARGET_ID);

        UserDetailedResponse response = service.getRequiredById(TARGET_ID);

        assertThat(response.nnuzb()).isEqualTo(FULL_NNUZB);
        verify(userRepo, never()).exists(any(Specification.class));
    }

    private static UserDetailedResponse detail(String nnuzb) {
        return new UserDetailedResponse(
                TARGET_ID, UUID.randomUUID(), "user", true, "Ali", "Valiyev", null, nnuzb, null,
                null, null, null, null, null, null, null, null
        );
    }
}
