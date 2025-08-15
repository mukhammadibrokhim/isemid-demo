package uz.uzinfocom.isemid.commons.exceptions;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");
    private final MessageUtil messageUtil;

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message(messageUtil.get("error.not_found"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message((ex.getReason()))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getDetailMessageCode())
                                .build()))
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message(messageUtil.get("error.method_not_supported", ex.getMethod(), Arrays.toString(ex.getSupportedMethods())))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.bad_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(getRootCauseMessage(ex))
                                .build()))
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        logger.error("Request body could not be read: ", ex);
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ApiErrorResponse> handleConversion(HttpMessageConversionException ex) {
        logger.error("Request body conversion error: ", ex);
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field("RequestBody")
                                .errorMessage(ex.getMostSpecificCause().getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getParameterName())
                                .errorMessage(messageUtil.get("validation.required", ex.getParameterName()))
                                .build()))
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        logger.error("Validation failed: ", ex);
        List<ApiErrorResponse.ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiErrorResponse.ErrorDetail.builder()
                        .field(error.getField())
                        .errorMessage(error.getDefaultMessage())
                        .build())
                .toList();

        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.bad_request"))
                        .errors(errors)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getName())
                                .errorMessage(messageUtil.get("validation.invalid_format", ex.getName()))
                                .build()))
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        logger.error("Authentication failed: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .message(messageUtil.get("auth.login_failed"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field("InvalidDataAccessApiUsageException")
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        logger.error("Unhandled exception: ", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(INTERNAL_SERVER_ERROR.value())
                        .message(messageUtil.get("error.internal_server"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }
}
