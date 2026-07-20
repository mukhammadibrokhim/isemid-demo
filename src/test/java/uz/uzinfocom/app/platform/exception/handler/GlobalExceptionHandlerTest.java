package uz.uzinfocom.app.platform.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.uzinfocom.app.platform.i18n.I18nConfig;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.shared.dto.response.ErrorResponse;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-level only: instantiates {@link GlobalExceptionHandler} directly with a real
 * {@link MessageResolver} (backed by the real i18n bundles, so message-key typos are caught)
 * and a mocked {@link TraceIdProvider}, mirroring the established convention in {@code
 * Api2ExceptionHandlerTest}.
 */
class GlobalExceptionHandlerTest {

    private static final String TRACE_ID = "trace-id-12345678";

    private final TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MessageResolver messages = new MessageResolver(new I18nConfig().messageSource());
        when(traceIdProvider.getOrCreate(any())).thenReturn(TRACE_ID);

        handler = new GlobalExceptionHandler(messages, traceIdProvider);
        request = new MockHttpServletRequest("GET", "/v1/dashboard/home");
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void appExceptionUsesItsOwnErrorCodeAndResolvesMessageWithArgs() {
        ScopeViolationException exception = new ScopeViolationException("dashboard.module.not_found", "unknown");

        ResponseEntity<ErrorResponse> response = handler.handleAppException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("SCOPE_VIOLATION");
        assertThat(response.getBody().message()).isEqualTo("Unknown dashboard module: unknown.");
        assertThat(response.getBody().traceId()).isEqualTo(TRACE_ID);
        assertThat(response.getBody().path()).isEqualTo("/v1/dashboard/home");
        assertThat(response.getBody().violations()).isEmpty();
    }

    @Test
    void constraintViolationSanitizesAndSortsFieldViolations() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Constrained>> violations = validator.validate(new Constrained());
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().violations()).hasSize(1);
        assertThat(response.getBody().violations().get(0).field()).isEqualTo("name");
    }

    @Test
    void bindExceptionMapsFieldErrorsToViolations() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new org.springframework.validation.FieldError("target", "email", "must not be blank"));
        BindException exception = new BindException(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleBindException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().violations()).hasSize(1);
        assertThat(response.getBody().violations().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().violations().get(0).message()).isEqualTo("must not be blank");
    }

    @Test
    void accessDeniedMapsToForbidden() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
    }

    @Test
    void authenticationExceptionMapsToUnauthorized() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuthentication(new BadCredentialsException("bad creds"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void missingServletRequestParameterReportsMissingNotTypeMismatch() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("organizationId", "Long");

        ResponseEntity<ErrorResponse> response =
                handler.handleMissingServletRequestParameter(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Request parameter is required.");
        assertThat(response.getBody().violations()).hasSize(1);
        assertThat(response.getBody().violations().get(0).field()).isEqualTo("organizationId");
    }

    @Test
    void missingRequestHeaderReportsMissingParameterMessage() {
        MissingRequestHeaderException exception = missingHeaderException("X-Source");

        ResponseEntity<ErrorResponse> response = handler.handleMissingRequestHeader(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Request parameter is required.");
        assertThat(response.getBody().violations().get(0).field()).isEqualTo("X-Source");
    }

    @Test
    void methodArgumentTypeMismatchStillReportsTypeMismatchMessage() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid request parameter type.");
        assertThat(response.getBody().violations().get(0).field()).isEqualTo("id");
    }

    @Test
    void noResourceFoundMapsToResourceNotFound() {
        NoResourceFoundException exception =
                new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/unknown", "/unknown");

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void methodNotSupportedMapsTo405() {
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"), request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void mediaTypeNotSupportedMapsTo415() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException("text/xml"), request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void mediaTypeNotAcceptableMapsTo406() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotAcceptable(
                new HttpMediaTypeNotAcceptableException("No acceptable representation"), request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
        assertThat(response.getBody().code()).isEqualTo("NOT_ACCEPTABLE");
    }

    @Test
    void httpMessageNotReadableDistinguishesMissingBodyFromMalformedJson() {
        HttpMessageNotReadableException missingBody = new HttpMessageNotReadableException(
                "Required request body is missing", (org.springframework.http.HttpInputMessage) null
        );
        HttpMessageNotReadableException malformed = new HttpMessageNotReadableException(
                "Unexpected character", (org.springframework.http.HttpInputMessage) null
        );

        ResponseEntity<ErrorResponse> missingBodyResponse = handler.handleHttpMessageNotReadable(missingBody, request);
        ResponseEntity<ErrorResponse> malformedResponse = handler.handleHttpMessageNotReadable(malformed, request);

        assertThat(missingBodyResponse.getBody().code()).isEqualTo("REQUEST_BODY_MISSING");
        assertThat(malformedResponse.getBody().code()).isEqualTo("MALFORMED_JSON");
    }

    @Test
    void illegalArgumentMapsToBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void dataIntegrityViolationMapsTo500WithDataIntegrityCode() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key"), request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("DATA_INTEGRITY");
    }

    @Test
    void asyncExecutorSaturationMapsToServiceUnavailableForBothExceptionTypes() {
        ResponseEntity<ErrorResponse> rejected =
                handler.handleAsyncExecutorSaturation(new RejectedExecutionException("full"), request);
        ResponseEntity<ErrorResponse> taskRejected =
                handler.handleAsyncExecutorSaturation(new TaskRejectedException("full"), request);

        assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(rejected.getBody().code()).isEqualTo("ASYNC_EXECUTOR_SATURATED");
        assertThat(taskRejected.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void unexpectedExceptionMapsTo500WithoutLeakingInternalMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new NullPointerException("some internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error occurred.");
    }

    @Test
    void unexpectedExceptionStillAttachesTechnicalDetailToRequestLogContext() {
        handler.handleUnexpected(new NullPointerException("some internal detail"), request);

        RequestLogErrorContext.ErrorDetails details = RequestLogErrorContext.get(request).orElseThrow();
        assertThat(details.errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(details.technicalMessage()).isEqualTo("some internal detail");
    }

    private MissingRequestHeaderException missingHeaderException(String headerName) {
        try {
            java.lang.reflect.Method method = GlobalExceptionHandlerTest.class
                    .getDeclaredMethod("dummyHeaderTarget", String.class);
            org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, 0);
            return new MissingRequestHeaderException(headerName, parameter);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    private void dummyHeaderTarget(String header) {
    }

    @Getter
    @Setter
    static class Constrained {
        @NotBlank
        private String name;
    }
}
