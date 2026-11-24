package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.i18n.I18nConfig;
import uz.uzinfocom.app.platform.i18n.LenientAcceptLanguageResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.ObservabilityProperties;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.security.handler.AccessDeniedMessageCodeResolver;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A token that validates but carries no ISEMID role (e.g. a DHP user with only the
 * provider's own roles) makes the IAM sync throw AccessDeniedException from inside the
 * authentication converter. That must surface as 403 FORBIDDEN with the localized
 * "no role assigned" message, never as a 401 that the client reads as "session expired".
 */
class ErrorResponseFilterRoleDenialTest {

    private final ErrorResponseWriter writer = new ErrorResponseWriter(
            JsonMapper.builder().build(),
            new MessageResolver(new I18nConfig().messageSource()),
            new LenientAcceptLanguageResolver(),
            traceIdProvider(),
            new ObservabilityProperties()
    );

    private final ErrorResponseFilter filter = new ErrorResponseFilter(
            writer,
            new JsonAuthenticationEntryPoint(writer),
            new JsonAccessDeniedHandler(writer, new AccessDeniedMessageCodeResolver())
    );

    private static TraceIdProvider traceIdProvider() {
        TraceIdProvider provider = mock(TraceIdProvider.class);
        when(provider.getOrCreate(org.mockito.ArgumentMatchers.any())).thenReturn("trace");
        return provider;
    }

    private MockHttpServletResponse run(String acceptLanguage, RuntimeException thrown) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/users/me");
        request.addHeader("Accept-Language", acceptLanguage);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw thrown;
        };
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void missingRoleIsForbiddenWithLocalizedMessage() throws Exception {
        MockHttpServletResponse response = run("uz", new AccessDeniedException("iam.role.not_assigned"));

        assertThat(response.getStatus()).isEqualTo(403);
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("FORBIDDEN").contains("Sizga YKEM tizimi uchun rol berilmagan.");
    }

    @Test
    void inactiveRoleIsForbiddenWithLocalizedMessage() throws Exception {
        MockHttpServletResponse response = run("uz-Cyrl", new AccessDeniedException("iam.role.inactive"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).contains("фаол эмас");
    }

    @Test
    void genuineAuthenticationFailureStaysUnauthorized() throws Exception {
        MockHttpServletResponse response = run("uz", new InsufficientAuthenticationException("no token"));

        assertThat(response.getStatus()).isEqualTo(401);
    }
}
