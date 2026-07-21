package uz.uzinfocom.app.platform.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.security.handler.AccessDeniedMessageCodeResolver;
import uz.uzinfocom.app.shared.dto.response.ErrorResponse;
import uz.uzinfocom.app.shared.dto.response.FieldViolationResponse;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final int MAX_VIOLATIONS = 100;
    private static final int MAX_FIELD_LENGTH = 100;
    private static final int MAX_VIOLATION_MESSAGE_LENGTH = 300;
    private static final Pattern SAFE_FIELD_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.\\-\\[\\]]{1,100}$");

    private final MessageResolver messages;
    private final TraceIdProvider traceIdProvider;
    private final AccessDeniedMessageCodeResolver accessDeniedMessageCodeResolver;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException exception,
            HttpServletRequest request
    ) {
        String message = messages.resolve(exception.getMessageCode(), exception.getArgs());
        return respond(exception.getErrorCode(), message, request, exception, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getBindingResult().getFieldErrors().stream()
                .limit(MAX_VIOLATIONS)
                .map(fieldError -> violation(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return respondValidationFailed(request, exception, violations);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getBindingResult().getFieldErrors().stream()
                .limit(MAX_VIOLATIONS)
                .map(fieldError -> violation(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return respondValidationFailed(request, exception, violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String parameterName = result.getMethodParameter().getParameterName();
                    return result.getResolvableErrors().stream()
                            .map(error -> violation(parameterName, error.getDefaultMessage()));
                })
                .limit(MAX_VIOLATIONS)
                .toList();

        return respondValidationFailed(request, exception, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .limit(MAX_VIOLATIONS)
                .map(constraint -> violation(
                        constraint.getPropertyPath().toString(),
                        constraint.getMessage()
                ))
                .toList();

        return respondValidationFailed(request, exception, violations);
    }

    /**
     * An {@link AccessDeniedException} thrown from application code inside a
     * controller method (as opposed to the Spring Security filter chain, which
     * is instead handled by {@code JsonAccessDeniedHandler}) reaches here via
     * MVC's own exception resolution. Both paths must apply the identical
     * message-code validation rule - see
     * {@link AccessDeniedMessageCodeResolver} - or a validated key thrown from
     * a filter forwards correctly while the same key thrown from a controller
     * body silently degrades to the generic forbidden message.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        String messageCode = accessDeniedMessageCodeResolver.resolve(exception.getMessage());
        String message = messageCode != null
                ? messages.resolve(messageCode)
                : messages.resolve(ErrorCode.FORBIDDEN.getDefaultMessageCode());

        return respond(ErrorCode.FORBIDDEN, message, request, exception, List.of());
    }

    /**
     * This usually catches AuthenticationException only if it is thrown inside MVC layer.
     * Spring Security filter-chain AuthenticationException must be handled by AuthenticationEntryPoint.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.UNAUTHORIZED, request, exception);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = List.of(violation(
                exception.getParameterName(),
                messages.resolve("validation.required")
        ));

        return respond(
                ErrorCode.BAD_REQUEST,
                messages.resolve("error.missing_parameter"),
                request,
                exception,
                violations
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = List.of(violation(
                exception.getHeaderName(),
                messages.resolve("validation.required")
        ));

        return respond(
                ErrorCode.BAD_REQUEST,
                messages.resolve("error.missing_parameter"),
                request,
                exception,
                violations
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = List.of(violation(
                exception.getName(),
                messages.resolve("validation.invalid_value")
        ));

        return respond(
                ErrorCode.BAD_REQUEST,
                messages.resolve("error.argument_type_mismatch"),
                request,
                exception,
                violations
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.RESOURCE_NOT_FOUND, request, exception);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, request, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.UNSUPPORTED_MEDIA_TYPE, request, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.NOT_ACCEPTABLE, request, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ErrorCode code = isMissingBody(exception)
                ? ErrorCode.REQUEST_BODY_MISSING
                : ErrorCode.MALFORMED_JSON;

        return respond(code, request, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.BAD_REQUEST, request, exception);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.DATA_INTEGRITY, request, exception);
    }

    @ExceptionHandler({RejectedExecutionException.class, TaskRejectedException.class})
    public ResponseEntity<ErrorResponse> handleAsyncExecutorSaturation(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        ErrorCode code = ErrorCode.ASYNC_EXECUTOR_SATURATED;
        attachLogError(request, code.getCode(), "Application async executor rejected the submitted task", exception);

        return ResponseEntity
                .status(code.getStatus())
                .body(error(code.getCode(), messages.resolve(code.getDefaultMessageCode()), request, List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return respond(ErrorCode.INTERNAL_ERROR, request, exception);
    }

    /**
     * Common path for handlers that report the exception via its own message and carry no
     * field violations. Most handlers below resolve to this.
     */
    private ResponseEntity<ErrorResponse> respond(
            ErrorCode code,
            HttpServletRequest request,
            Exception exception
    ) {
        return respond(code, messages.resolve(code.getDefaultMessageCode()), request, exception, List.of());
    }

    private ResponseEntity<ErrorResponse> respond(
            ErrorCode code,
            String message,
            HttpServletRequest request,
            Exception exception,
            List<FieldViolationResponse> violations
    ) {
        attachLogError(request, code.getCode(), exception);

        return ResponseEntity
                .status(code.getStatus())
                .body(error(code.getCode(), message, request, violations));
    }

    /**
     * Shared path for the four Bean Validation / binding exception types, which all resolve to
     * {@link ErrorCode#VALIDATION_FAILED} and differ only in how the field violations are extracted.
     */
    private ResponseEntity<ErrorResponse> respondValidationFailed(
            HttpServletRequest request,
            Exception exception,
            List<FieldViolationResponse> violations
    ) {
        attachLogError(request, ErrorCode.VALIDATION_FAILED.getCode(), firstViolationMessage(violations), exception);

        return ResponseEntity
                .badRequest()
                .body(error(
                        ErrorCode.VALIDATION_FAILED.getCode(),
                        messages.resolve(ErrorCode.VALIDATION_FAILED.getDefaultMessageCode()),
                        request,
                        violations
                ));
    }

    private ErrorResponse error(
            String code,
            String message,
            HttpServletRequest request,
            List<FieldViolationResponse> violations
    ) {
        String traceId = traceIdProvider.getOrCreate(request);

        return ErrorResponse.of(code, message, traceId, request.getRequestURI(), violations);
    }

    private void attachLogError(
            HttpServletRequest request,
            String errorCode,
            Exception exception
    ) {
        RequestLogErrorContext.attach(
                request,
                errorCode,
                extractExceptionMessage(exception),
                exception
        );
    }

    private void attachLogError(
            HttpServletRequest request,
            String errorCode,
            String message,
            Exception exception
    ) {
        RequestLogErrorContext.attach(request, errorCode, normalizeMessage(message), exception);
    }

    private String firstViolationMessage(List<FieldViolationResponse> violations) {
        return violations.stream()
                .findFirst()
                .map(violation -> violation.field() + ": " + normalizeMessage(violation.message()))
                .orElse("Validation failed");
    }

    private FieldViolationResponse violation(String field, String message) {
        return new FieldViolationResponse(safeField(field), safeViolationMessage(message));
    }

    private String safeField(String field) {
        if (field == null) {
            return "request";
        }
        String normalized = field.trim();
        if (normalized.length() > MAX_FIELD_LENGTH || !SAFE_FIELD_PATTERN.matcher(normalized).matches()) {
            return "request";
        }
        return normalized;
    }

    private String safeViolationMessage(String message) {
        if (message == null || message.isBlank()) {
            return messages.resolve("validation.invalid_value");
        }
        if (message.contains("Failed to convert")
                || message.contains("rejected value")
                || message.contains("java.")) {
            return messages.resolve("validation.invalid_value");
        }
        StringBuilder safe = new StringBuilder(Math.min(message.length(), MAX_VIOLATION_MESSAGE_LENGTH));
        for (int index = 0; index < message.length() && safe.length() < MAX_VIOLATION_MESSAGE_LENGTH; index++) {
            char character = message.charAt(index);
            safe.append(Character.isISOControl(character) ? ' ' : character);
        }
        String normalized = safe.toString().trim();
        return normalized.isEmpty() ? messages.resolve("validation.invalid_value") : normalized;
    }

    private String extractExceptionMessage(Exception exception) {
        if (exception == null) {
            return "No message";
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        Throwable cause = exception.getCause();

        while (cause != null) {
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                return cause.getMessage();
            }

            cause = cause.getCause();
        }

        return exception.getClass().getSimpleName();
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "No message";
        }

        return message;
    }

    private boolean isMissingBody(HttpMessageNotReadableException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("Required request body is missing");
    }
}
