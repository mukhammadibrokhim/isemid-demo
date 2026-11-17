package uz.uzinfocom.app.platform.security.handler;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.i18n.I18nConfig;
import uz.uzinfocom.app.platform.i18n.LenientAcceptLanguageResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.ObservabilityProperties;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The Security-filter-phase 403 must be localized from the request's own
 * Accept-Language, whatever LocaleContextHolder holds at that point (it holds the
 * servlet container's locale there, which does not know uz-Cyrl).
 */
class JsonAccessDeniedHandlerLocaleTest {

    private final JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler(
            new ErrorResponseWriter(
                    JsonMapper.builder().build(),
                    new MessageResolver(new I18nConfig().messageSource()),
                    new LenientAcceptLanguageResolver(),
                    traceIdProvider(),
                    new ObservabilityProperties()
            ),
            new AccessDeniedMessageCodeResolver()
    );

    private static TraceIdProvider traceIdProvider() {
        TraceIdProvider provider = mock(TraceIdProvider.class);
        when(provider.getOrCreate(org.mockito.ArgumentMatchers.any())).thenReturn("trace");
        return provider;
    }

    private String body(String acceptLanguage, String exceptionMessage) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/users/me");
        if (acceptLanguage != null) {
            request.addHeader("Accept-Language", acceptLanguage);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        try {
            handler.handle(request, response, new AccessDeniedException(exceptionMessage));
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
        assertThat(response.getStatus()).isEqualTo(403);
        return response.getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void genericForbiddenFollowsAcceptLanguage() throws Exception {
        assertThat(body("uz-Cyrl", "Access Denied")).contains("Рухсат берилмаган.");
        assertThat(body("uz", "Access Denied")).contains("Ruxsat berilmagan.");
        assertThat(body("ru", "Access Denied")).contains("Доступ запрещён.");
        assertThat(body("en", "Access Denied")).contains("Access denied.");
    }

    @Test
    void missingRoleMessageNamesYkemSystemPerLocale() throws Exception {
        assertThat(body("uz-Cyrl", "iam.role.not_assigned"))
                .contains("Сизга YKEM тизими учун рол берилмаган.");
        assertThat(body("uz", "iam.role.not_assigned"))
                .contains("Sizga YKEM tizimi uchun rol berilmagan.");
        assertThat(body("uz-Cyrl", "iam.role.inactive")).contains("фаол эмас");
    }
}
