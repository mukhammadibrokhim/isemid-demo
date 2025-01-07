package uz.uzinfocom.isemid.exceptioins;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
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
