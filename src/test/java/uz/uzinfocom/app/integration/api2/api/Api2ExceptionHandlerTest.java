package uz.uzinfocom.app.integration.api2.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.integration.api2.api.dto.Api2ErrorResponse;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenLookupValidationException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.support.Api2ErrorDecoder;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBody;
import uz.uzinfocom.app.platform.i18n.I18nConfig;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.TraceContext;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class Api2ExceptionHandlerTest {

    private static final String TRACE_ID = "trace-id-12345678";

    private final MessageSource messageSource = new I18nConfig().messageSource();
    private final MessageResolver messages = new MessageResolver(messageSource);
    private final Api2ExceptionHandler handler = new Api2ExceptionHandler(messages, new TraceIdProvider());
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Api2ErrorDecoder decoder = new Api2ErrorDecoder();

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void returnsLocalizedBadRequestWithUpstreamMetadata() throws Exception {
        Locale locale = Locale.ENGLISH;
        LocaleContextHolder.setLocale(locale);
        Api2Exception exception = decoder.decodeCitizen(
                "CITIZEN_NNUZB_LOOKUP",
                HttpStatusCode.valueOf(400),
                new Api2ResponseBody(
                        jsonMapper.readTree("{\"code\":\"BAD_INPUT\",\"message\":\"Invalid request\",\"detail\":\"field is invalid\"}"),
                        null,
                        org.springframework.http.MediaType.APPLICATION_JSON,
                        false,
                        false
                )
        );

        ResponseEntity<Api2ErrorResponse> response = handler.handleApi2Exception(
                exception,
                request("/v1/citizen")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo(messageSource.getMessage("api2.error.bad_request", null, locale));
        assertThat(response.getBody().errorCode()).isEqualTo("API2_UPSTREAM_BAD_REQUEST");
        assertThat(response.getBody().upstreamStatus()).isEqualTo(400);
        assertThat(response.getBody().upstreamCode()).isEqualTo("BAD_INPUT");
        assertThat(response.getBody().upstreamMessage()).isEqualTo("Invalid request");
        assertThat(response.getBody().upstreamDetail()).isEqualTo("field is invalid");
        assertThat(response.getBody().traceId()).isEqualTo(TRACE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"uz", "ru", "uz-Cyrl", "kaa"})
    void localizesValidationMessageAndFieldErrorsForSupportedLocales(String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);
        LocaleContextHolder.setLocale(locale);

        ResponseEntity<Api2ErrorResponse> response = handler.handleApi2Exception(
                new CitizenLookupValidationException(List.of(new FieldValidationError(
                        "nnuzb",
                        "validation.nnuzb.format"
                ))),
                request("/v1/citizen")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo(messageSource.getMessage("api2.citizen.error.validation", null, locale))
                .isNotEqualTo("api2.citizen.error.validation");
        assertThat(response.getBody().fieldErrors()).singleElement()
                .satisfies(fieldError -> {
                    assertThat(fieldError.field()).isEqualTo("nnuzb");
                    assertThat(fieldError.message())
                            .isEqualTo(messageSource.getMessage("validation.nnuzb.format", null, locale))
                            .isNotEqualTo("validation.nnuzb.format");
                });
        assertThat(response.getBody().traceId()).isEqualTo(TRACE_ID);
    }

    @Test
    void typeMismatchOnLegalEntityRequestKeepsLegalEntityClassification() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "tin",
                null,
                new IllegalArgumentException("bad tin")
        );

        ResponseEntity<Api2ErrorResponse> response = handler.handleTypeMismatch(
                exception,
                request("/v1/legal-entity")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("LEGAL_ENTITY_VALIDATION_FAILED");
        assertThat(response.getBody().operation()).isEqualTo("LEGAL_ENTITY_TIN_LOOKUP");
        assertThat(response.getBody().message())
                .isEqualTo(messageSource.getMessage("api2.legal_entity.error.validation", null, Locale.ENGLISH));
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setAttribute(TraceContext.REQUEST_ATTRIBUTE, TRACE_ID);
        return request;
    }
}
