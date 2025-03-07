package uz.uzinfocom.isemid.exceptioins;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger("uz.uzinfocom.isemid");

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ErrorResponse.builder().message(ex.getMessage())
                .success(false)
                .statusCode(ex.getStatusCode().value())
                .message(ex.getMessage())
                .errors(List.of(ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getDetailMessageCode())
                        .build()))
                .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(BAD_REQUEST.value()).body(ErrorResponse.builder()
                .success(false)
                .statusCode(BAD_REQUEST.value())
                .message("Missing required parameter: " + ex.getParameterName())
                .errors(List.of(ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage("Missing required parameter: " + ex.getParameterName())
                        .build()))
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN.value()).body(ErrorResponse.builder()
                .success(false)
                .statusCode(FORBIDDEN.value())
                .message(ex.getMessage())
                .errors(Collections.singletonList(ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getCause().getMessage())
                        .build()))
                .build());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ErrorResponse.builder()
                        .success(false)
                        .statusCode(ex.getStatusCode().value())
                        .message(ex.getReason())
                        .errors(List.of(ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getDetailMessageCode())
                                .build()))
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Invalid request body")
                .errors(List.of(new ErrorDetail(ex.getClass().getSimpleName(), ex.getLocalizedMessage())))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .success(false)
                .statusCode(METHOD_NOT_ALLOWED.value())
                .message(ex.getLocalizedMessage())
                .errors(List.of(new ErrorDetail(ex.getClass().getSimpleName(), ex.getLocalizedMessage())))
                .build(), METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation error")
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Type mismatch")
                .errors(List.of(new ErrorDetail(null, ex.getLocalizedMessage())))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .field(ex.getClass().getSimpleName())
                .errorMessage("Missing required header: " +  ex.getHeaderName())
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Request header is missing")
                .errors(Collections.singletonList(errorDetail))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.builder()
                        .success(false)
                        .statusCode(NOT_FOUND.value())
                        .message("Username not found")
                        .errors(List.of(ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage("User not found: " + ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .success(false)
                        .statusCode(UNAUTHORIZED.value())
                        .message("Неверное имя пользователя или пароль")
                        .errors(List.of(ErrorDetail.builder()
                                .field(ex.getClass().getSimpleName())
                                .errorMessage(ex.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Exception", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .success(false)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .errors(List.of(ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getLocalizedMessage())
                        .build()))
                .build());
    }
}
