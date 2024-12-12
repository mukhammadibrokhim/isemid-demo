package com.abdulatif.emis.exception;

import com.abdulatif.emis.model.util.ResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("result", request.getDescription(false));
        errorDetails.put("errorMessage", ex.getMessage());
        if (ex.getMessage().equals("Access is denied") || ex.getMessage().equals("Доступ запрещен")) {
            logger.error("##########################" + ex.getMessage() +"#############################");
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.EXPECTATION_FAILED, 417, "So'rov kutilganidek emas....", errorDetails), HttpStatus.EXPECTATION_FAILED);
        } else
            return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.INTERNAL_SERVER_ERROR, 500, "Kutilmagan xatolik yuz berdi...", errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(RecordNotFoundException ex) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.NOT_FOUND, 404, ex.getMessage(), null), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RecordBadRequestException.class)
    public final ResponseEntity<Object> handleBadRequestException(RecordBadRequestException ex) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.BAD_REQUEST, 400, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RecordForbiddenException.class)
    public final ResponseEntity<Object> handleForbiddenRequestException(RecordForbiddenException ex) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.FORBIDDEN, 403, ex.getMessage(), null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnsupportedMediaType.class)
    public final ResponseEntity<Object> handleUnsupportedMediaType(UnsupportedMediaType ex) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.UNSUPPORTED_MEDIA_TYPE, 415, ex.getMessage(), null), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<Object> handleUnsupportedMediaType(ConflictException ex) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.CONFLICT, 409, ex.getMessage(), null), HttpStatus.CONFLICT);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<String> details = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.BAD_REQUEST, 400, "Validation Failed", details), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DeleteException.class)
    public final ResponseEntity<Object> deleteExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.CONFLICT, 409, ex.getMessage(), null), HttpStatus.CONFLICT);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<String> errorDetails = new ArrayList<>();
        errorDetails.add(request.getDescription(false));
        if (ex.getMessage().equals("Access is denied") || ex.getMessage().equals("Доступ запрещен")) {
            logger.error("##########################MESSAGE NOT READABLE#############################");
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.EXPECTATION_FAILED, 417, ex.getMessage(), errorDetails), HttpStatus.EXPECTATION_FAILED);
        } else
            return new ResponseEntity(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.BAD_REQUEST, 400, ex.getMessage(), errorDetails), HttpStatus.BAD_REQUEST);
    }
}
