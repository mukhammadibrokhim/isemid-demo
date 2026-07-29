package uz.uzinfocom.app.platform.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.settings.application.RouteAccessPolicyResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicRouteAuthorizationManagerTest {

    private final RouteAccessPolicyResolver routeAccessPolicyResolver = mock(RouteAccessPolicyResolver.class);
    private final DynamicRouteAuthorizationManager manager =
            new DynamicRouteAuthorizationManager(routeAccessPolicyResolver);

    private RequestAuthorizationContext context(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRequestURI(path);
        return new RequestAuthorizationContext(request);
    }

    @Test
    void grantsAnOpenPathRegardlessOfAuthenticationState() {
        when(routeAccessPolicyResolver.resolve("/v1/auth/login/sso-web"))
                .thenReturn(RequestPolicy.publicRoute("/v1/auth/**"));

        AuthorizationResult result = manager.authorize(() -> null, context("/v1/auth/login/sso-web"));

        assertThat(result.isGranted()).isTrue();
    }

    @Test
    void deniesANonOpenPathWithNoAuthentication() {
        when(routeAccessPolicyResolver.resolve("/v1/users/me"))
                .thenReturn(RequestPolicy.defaultProtectedRoute());

        AuthorizationResult result = manager.authorize(() -> null, context("/v1/users/me"));

        assertThat(result.isGranted()).isFalse();
    }

    @Test
    void deniesANonOpenPathForSpringSecuritysBuiltInAnonymousToken() {
        // Regression test: Spring Security's AnonymousAuthenticationFilter gives every
        // unauthenticated request a non-null AnonymousAuthenticationToken whose
        // isAuthenticated() is true by design - a naive isAuthenticated() check would
        // wrongly grant access here. This exact bug shipped once and was caught live
        // (curl against /v1/users/me anonymously returned 500 instead of 401).
        when(routeAccessPolicyResolver.resolve("/v1/admin/settings"))
                .thenReturn(RequestPolicy.defaultProtectedRoute());

        Authentication anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        assertThat(anonymous.isAuthenticated()).isTrue();

        AuthorizationResult result = manager.authorize(() -> anonymous, context("/v1/admin/settings"));

        assertThat(result.isGranted()).isFalse();
    }

    @Test
    void grantsANonOpenPathWhenTheCallerIsAuthenticated() {
        when(routeAccessPolicyResolver.resolve("/v1/users/me"))
                .thenReturn(RequestPolicy.defaultProtectedRoute());

        Authentication authenticated = mock(Authentication.class);
        when(authenticated.isAuthenticated()).thenReturn(true);

        AuthorizationResult result = manager.authorize(() -> authenticated, context("/v1/users/me"));

        assertThat(result.isGranted()).isTrue();
    }
}
