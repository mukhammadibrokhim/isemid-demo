package uz.uzinfocom.app.platform.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
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
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.FieldViolationResponse;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageResolver messages;
    private final TraceIdProvider traceIdProvider;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException exception,
            HttpServletRequest request
    ) {
        ErrorCode code = exception.getErrorCode();
        String message = messages.resolve(exception.getMessageCode(), exception.getArgs());
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Application exception handled. traceId={}, exception={}, errorCode={}, messageCode={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                code.getCode(),
                exception.getMessageCode(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(code.getStatus())
                .body(error(code.getCode(), message, request, List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldViolationResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Method argument validation failed. traceId={}, exception={}, method={}, path={}, fields={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                violations.stream().map(FieldViolationResponse::field).toList()
        );

        String message = messages.resolve(ErrorCode.VALIDATION_FAILED.getDefaultMessageCode());

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.VALIDATION_FAILED.getCode(), message, request, violations));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new FieldViolationResponse(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Request binding validation failed. traceId={}, exception={}, method={}, path={}, fields={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                violations.stream().map(FieldViolationResponse::field).toList()
        );

        String message = messages.resolve(ErrorCode.VALIDATION_FAILED.getDefaultMessageCode());

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.VALIDATION_FAILED.getCode(), message, request, violations));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<String> fieldNames = exception.getParameterValidationResults()
                .stream()
                .map(result -> result.getMethodParameter().getParameterName())
                .toList();
        String fieldName = fieldNames.isEmpty() ? "methodValidation" : String.join(",", fieldNames);
        List<FieldViolationResponse> violations = exception.getAllErrors()
                .stream()
                .map(error -> new FieldViolationResponse(
                        fieldName,
                        error.getDefaultMessage()
                ))
                .toList();

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Handler method validation failed. traceId={}, exception={}, method={}, path={}, fields={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                fieldNames
        );

        String message = messages.resolve(ErrorCode.VALIDATION_FAILED.getDefaultMessageCode());

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.VALIDATION_FAILED.getCode(), message, request, violations));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldViolationResponse> violations = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldViolationResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Constraint validation failed. traceId={}, exception={}, method={}, path={}, fields={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                violations.stream().map(FieldViolationResponse::field).toList()
        );

        String message = messages.resolve(ErrorCode.VALIDATION_FAILED.getDefaultMessageCode());

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.VALIDATION_FAILED.getCode(), message, request, violations));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Access denied. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        String message = messages.resolve(ErrorCode.FORBIDDEN.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error(ErrorCode.FORBIDDEN.getCode(), message, request, List.of()));
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
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Authentication failed in MVC layer. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        String message = messages.resolve(ErrorCode.UNAUTHORIZED.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error(ErrorCode.UNAUTHORIZED.getCode(), message, request, List.of()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String message = messages.resolve("error.argument_type_mismatch");
        List<FieldViolationResponse> violations = List.of(new FieldViolationResponse(
                exception.getParameterName(),
                exception.getMessage()
        ));
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Required request parameter is missing. traceId={}, exception={}, method={}, path={}, parameter={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getParameterName(),
                exception.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.BAD_REQUEST.getCode(), message, request, violations));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
        String message = messages.resolve(ErrorCode.BAD_REQUEST.getDefaultMessageCode());
        List<FieldViolationResponse> violations = List.of(new FieldViolationResponse(
                exception.getHeaderName(),
                exception.getMessage()
        ));
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Required request header is missing. traceId={}, exception={}, method={}, path={}, header={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getHeaderName(),
                exception.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.BAD_REQUEST.getCode(), message, request, violations));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = messages.resolve(ErrorCode.BAD_REQUEST.getDefaultMessageCode());
        List<FieldViolationResponse> violations = List.of(new FieldViolationResponse(
                exception.getName(),
                exception.getMessage()
        ));
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Request argument type mismatch. traceId={}, exception={}, method={}, path={}, argument={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getName(),
                exception.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(error(ErrorCode.BAD_REQUEST.getCode(), message, request, violations));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "No resource found. traceId={}, exception={}, method={}, path={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI()
        );

        String message = messages.resolve(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error(
                        ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                        message,
                        request,
                        List.of()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "HTTP method not supported. traceId={}, exception={}, method={}, path={}, supportedMethods={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getSupportedHttpMethods()
        );

        String message = messages.resolve(ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error(
                        ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        message,
                        request,
                        List.of()
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "HTTP media type not supported. traceId={}, exception={}, method={}, path={}, contentType={}, supportedMediaTypes={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getContentType(),
                exception.getSupportedMediaTypes()
        );

        String message = messages.resolve(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(error(
                        ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                        message,
                        request,
                        List.of()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        // Keep request bodies and headers out of logs; malformed payloads can contain sensitive values.
        log.warn(
                "HTTP request body is missing or malformed. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorCode code = isMissingBody(exception)
                ? ErrorCode.REQUEST_BODY_MISSING
                : ErrorCode.MALFORMED_JSON;

        String message = messages.resolve(code.getDefaultMessageCode());

        return ResponseEntity
                .status(code.getStatus())
                .body(error(code.getCode(), message, request, List.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Illegal argument. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage()
        );

        ErrorCode code = ErrorCode.BAD_REQUEST;
        String message = messages.resolve(code.getDefaultMessageCode());

        return ResponseEntity
                .status(code.getStatus())
                .body(error(code.getCode(), message, request, List.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        log.error(
                "Database integrity violation. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );

        String message = messages.resolve(ErrorCode.DATA_INTEGRITY.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(ErrorCode.DATA_INTEGRITY.getCode(), message, request, List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        // Fallback response stays generic; stack trace is logged for diagnosis without exposing details to clients.
        log.error(
                "Unhandled application exception. traceId={}, exception={}, method={}, path={}, message={}",
                traceId,
                exception.getClass().getSimpleName(),
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );

        String message = messages.resolve(ErrorCode.INTERNAL_ERROR.getDefaultMessageCode());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(ErrorCode.INTERNAL_ERROR.getCode(), message, request, List.of()));
    }

    private ErrorResponse error(
            String code,
            String message,
            HttpServletRequest request,
            List<FieldViolationResponse> violations
    ) {
        String traceId = traceIdProvider.getTraceId(request);

        return ErrorResponse.of(code, message, traceId, request.getRequestURI(), violations);
    }

    private boolean isMissingBody(HttpMessageNotReadableException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("Required request body is missing");
    }
}
