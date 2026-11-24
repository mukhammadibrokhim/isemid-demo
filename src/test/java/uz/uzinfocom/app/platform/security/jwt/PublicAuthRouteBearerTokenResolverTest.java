package uz.uzinfocom.app.platform.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicAuthRouteBearerTokenResolverTest {

    private final PublicAuthRouteBearerTokenResolver resolver = new PublicAuthRouteBearerTokenResolver();

    @Test
    void ignoresBearerHeaderOnAuthRoutes() {
        assertThat(resolver.resolve(request("/v1/auth/logout/sso-web"))).isNull();
        assertThat(resolver.resolve(request("/v1/auth/refresh/sso-web"))).isNull();
        assertThat(resolver.resolve(request("/v1/auth/login/dhp-web"))).isNull();
    }

    @Test
    void resolvesBearerHeaderOnOtherRoutes() {
        assertThat(resolver.resolve(request("/v1/user/me"))).isEqualTo("token-value");
        assertThat(resolver.resolve(request("/v1/authors"))).isEqualTo("token-value");
    }

    @Test
    void stripsContextPathBeforeMatching() {
        MockHttpServletRequest request = request("/api/v1/auth/logout/sso-web");
        request.setContextPath("/api");

        assertThat(resolver.resolve(request)).isNull();
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.addHeader("Authorization", "Bearer token-value");
        return request;
    }
}
