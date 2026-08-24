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
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");
    private final MessageUtil messageUtil;

    // 404 – resurs topilmadi
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoResourceFoundException ex) {
        logger.warn("❌ NoResourceFoundException: {}", ex.getMessage(), ex);
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

    // ApiException (custom biznes xatoliklar)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        logger.error("⚠️ ApiException ({}): {}", ex.getStatusCode(), ex.getReason(), ex);
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message(ex.getReason())
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getDetailMessageCode())
                                .build()))
                        .build());
    }

    // LIS protokoli akt uchun hali biriktirilmagan
    @ExceptionHandler(ProtocolNotReadyException.class)
    public ResponseEntity<ApiErrorResponse> handleProtocolNotReady(ProtocolNotReadyException ex) {
        logger.warn("⚠️ ProtocolNotReadyException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(BAD_REQUEST.value())
                        .message(messageUtil.get("error.invalid_request.lis_protocol_not_attached"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    // HTTP metod mos emas
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        logger.warn("⚠️ HttpRequestMethodNotSupportedException: method={}, supported={}", ex.getMethod(), ex.getSupportedMethods(), ex);
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

    // Ma'lumot bazasida cheklov (constraint) buzilgan
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("❌ DataIntegrityViolationException: {}", getRootCauseMessage(ex), ex);
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

    // Request JSON not readable
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        logger.error("⚠️ HttpMessageNotReadableException (Request body could not be read): {}", ex.getMessage(), ex);
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

    // JSON field conversion error
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ApiErrorResponse> handleConversion(HttpMessageConversionException ex) {
        logger.error("⚠️ HttpMessageConversionException: {}", ex.getMessage(), ex);
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

    // Request param yetishmayapti
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        logger.warn("⚠️ MissingServletRequestParameterException: {}", ex.getParameterName(), ex);
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

    // DTO validation xatoliklari
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        logger.warn("⚠️ MethodArgumentNotValidException: {} errors", ex.getErrorCount(), ex);
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

    // Parametr tipi mos emas (masalan: LocalDate kutilgan, String keldi)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.warn("⚠️ MethodArgumentTypeMismatchException: param={} type={}", ex.getName(), ex.getRequiredType(), ex);
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

    // Login parol xato
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("🚫 BadCredentialsException: {}", ex.getMessage(), ex);
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

    // Noto‘g‘ri repository ishlatilishi yoki query
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        logger.error("❌ InvalidDataAccessApiUsageException: {}", ex.getMessage(), ex);
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

    // Avtorizatsiya rad etilgan (403)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        logger.warn("🚫 AuthorizationDeniedException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(FORBIDDEN)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(FORBIDDEN.value())
                        .message(messageUtil.get("error.forbidden"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field("AuthorizationDeniedException")
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    // Boshqa barcha xatoliklar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        logger.error("💥 Unhandled Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(INTERNAL_SERVER_ERROR.value())
                        .message(messageUtil.get("error.internal_server"))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getLocalizedMessage())
                                .build()))
                        .build());
    }
}
