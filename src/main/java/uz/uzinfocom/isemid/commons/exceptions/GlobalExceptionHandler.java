package uz.uzinfocom.isemid.commons.exceptions;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message("Запрашиваемый ресурс не найден или не существует.")
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getDetailMessageCode())
                                .build()))
                        .build());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message("Метод " + ex.getMethod() + " не поддерживается! Доступные методы: " + Arrays.toString(ex.getSupportedMethods()))
                        .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getDetailMessageCode())
                                .build()))
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.error("Request body could not be read: ", ex);
        return ResponseEntity.badRequest().body(ApiErrorResponse.builder()
                .success(false)
                .statusCode(BAD_REQUEST.value())
                .message("Неверный формат JSON")
                .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getLocalizedMessage())
                        .build()))
                .build());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageConversionException(HttpMessageConversionException ex) {
        logger.error("Request body conversion error: ", ex);
        return ResponseEntity.status(BAD_REQUEST).body(ApiErrorResponse.builder()
                .success(false)
                .statusCode(BAD_REQUEST.value())
                .message("Неверный формат сообщения")
                .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getLocalizedMessage())
                        .build()))
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        logger.error("Bad Credentials Exception", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getLocalizedMessage())
                .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getLocalizedMessage())
                        .build()))
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        logger.error("Exception", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ApiErrorResponse.builder()
                .success(false)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .message(MessageUtil.INTERNAL_SERVER_ERROR)
                .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getLocalizedMessage())
                        .build()))
                .build());
    }
}
