package uz.uzinfocom.app.integration.api2.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Api2ExceptionHandlerTest {

    private static final String TRACE_ID =
            "trace-id-12345678";

    private static final String CITIZEN_PATH =
            "/v1/citizen";

    private static final String LEGAL_ENTITY_PATH =
            "/v1/legal-entity";

    private MessageSource messageSource;
    private Api2ExceptionHandler handler;
    private JsonMapper jsonMapper;
    private Api2ErrorDecoder decoder;

    @BeforeEach
    void setUp() {
        messageSource = new I18nConfig().messageSource();

        MessageResolver messages =
                new MessageResolver(messageSource);

        TraceIdProvider traceIdProvider =
                mock(TraceIdProvider.class);

        when(traceIdProvider.getTraceId(
                any(HttpServletRequest.class)
        )).thenAnswer(invocation -> {
            HttpServletRequest request =
                    invocation.getArgument(0);

            Object traceId = request.getAttribute(
                    TraceContext.REQUEST_ATTRIBUTE
            );

            return traceId instanceof String value
                    ? value
                    : "N/A";
        });

        handler = new Api2ExceptionHandler(
                messages,
                traceIdProvider
        );

        jsonMapper = JsonMapper.builder().build();
        decoder = new Api2ErrorDecoder();
    }

    @AfterEach
    void tearDown() {
        /*
         * LocaleContextHolder va MDC thread-local.
         * Bir test contexti keyingi testga o'tmasligi kerak.
         */
        LocaleContextHolder.resetLocaleContext();
        MDC.remove(TraceContext.MDC_KEY);
    }

    @Test
    void returnsLocalizedBadRequestWithUpstreamMetadata()
            throws Exception {

        Locale locale = Locale.ENGLISH;
        LocaleContextHolder.setLocale(locale);

        Api2Exception exception = decoder.decodeCitizen(
                "CITIZEN_NNUZB_LOOKUP",
                HttpStatusCode.valueOf(400),
                new Api2ResponseBody(
                        jsonMapper.readTree("""
                                {
                                  "code": "BAD_INPUT",
                                  "message": "Invalid request",
                                  "detail": "field is invalid"
                                }
                                """),
                        null,
                        org.springframework.http.MediaType.APPLICATION_JSON,
                        false,
                        false
                )
        );

        ResponseEntity<Api2ErrorResponse> response =
                handler.handleApi2Exception(
                        exception,
                        request(CITIZEN_PATH)
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Api2ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo(messageSource.getMessage(
                        "api2.error.bad_request",
                        null,
                        locale
                ));

        assertThat(body.errorCode())
                .isEqualTo("API2_UPSTREAM_BAD_REQUEST");

        assertThat(body.upstreamStatus())
                .isEqualTo(400);

        assertThat(body.upstreamCode())
                .isEqualTo("BAD_INPUT");

        assertThat(body.upstreamMessage())
                .isEqualTo("Invalid request");

        assertThat(body.upstreamDetail())
                .isEqualTo("field is invalid");

        assertThat(body.traceId())
                .isEqualTo(TRACE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "uz",
            "ru",
            "uz-Cyrl",
            "kaa"
    })
    void localizesValidationMessageAndFieldErrorsForSupportedLocales(
            String languageTag
    ) {
        Locale locale = Locale.forLanguageTag(languageTag);
        LocaleContextHolder.setLocale(locale);

        CitizenLookupValidationException exception =
                new CitizenLookupValidationException(
                        List.of(
                                new FieldValidationError(
                                        "nnuzb",
                                        "validation.nnuzb.format"
                                )
                        )
                );

        ResponseEntity<Api2ErrorResponse> response =
                handler.handleApi2Exception(
                        exception,
                        request(CITIZEN_PATH)
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Api2ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.message())
                .isEqualTo(messageSource.getMessage(
                        "api2.citizen.error.validation",
                        null,
                        locale
                ))
                .isNotEqualTo(
                        "api2.citizen.error.validation"
                );

        assertThat(body.fieldErrors())
                .singleElement()
                .satisfies(fieldError -> {
                    assertThat(fieldError.field())
                            .isEqualTo("nnuzb");

                    assertThat(fieldError.message())
                            .isEqualTo(
                                    messageSource.getMessage(
                                            "validation.nnuzb.format",
                                            null,
                                            locale
                                    )
                            )
                            .isNotEqualTo(
                                    "validation.nnuzb.format"
                            );
                });

        assertThat(body.traceId())
                .isEqualTo(TRACE_ID);
    }

    @Test
    void typeMismatchOnLegalEntityRequestKeepsLegalEntityClassification() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        "abc",
                        Integer.class,
                        "tin",
                        legalEntityTinParameter(),
                        new IllegalArgumentException(
                                "Invalid TIN value"
                        )
                );

        ResponseEntity<Api2ErrorResponse> response =
                handler.handleTypeMismatch(
                        exception,
                        request(LEGAL_ENTITY_PATH)
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        Api2ErrorResponse body = response.getBody();

        assertThat(body).isNotNull();

        assertThat(body.errorCode())
                .isEqualTo(
                        "LEGAL_ENTITY_VALIDATION_FAILED"
                );

        assertThat(body.operation())
                .isEqualTo(
                        "LEGAL_ENTITY_TIN_LOOKUP"
                );

        assertThat(body.message())
                .isEqualTo(messageSource.getMessage(
                        "api2.legal_entity.error.validation",
                        null,
                        Locale.ENGLISH
                ));

        assertThat(body.traceId())
                .isEqualTo(TRACE_ID);
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", uri);

        request.setAttribute(
                TraceContext.REQUEST_ATTRIBUTE,
                TRACE_ID
        );

        return request;
    }

    private MethodParameter legalEntityTinParameter() {
        try {
            Method method = HandlerMethodFixture.class
                    .getDeclaredMethod(
                            "lookupLegalEntity",
                            Integer.class
                    );

            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "Failed to create test MethodParameter",
                    exception
            );
        }
    }

    /**
     * MethodArgumentTypeMismatchException real MVC metadata talab qiladi.
     * Ushbu fixture test uchun haqiqiy MethodParameter beradi.
     */
    private static final class HandlerMethodFixture {

        @SuppressWarnings("unused")
        void lookupLegalEntity(Integer tin) {
            // Test fixture method.
        }
    }
}