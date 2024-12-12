package com.abdulatif.emis.exception;

import com.abdulatif.emis.model.util.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorDetails.class)
    public ResponseEntity<?> resourceNotFoundHandling(ErrorDetails exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.NOT_FOUND, 404, exception.getMessage(), errorDetails), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandling(Exception exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), request.getDescription(false));

        if (exception.getMessage().toLowerCase().contains("not found")) {
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.NOT_FOUND, 404, exception.getMessage(), null), HttpStatus.NOT_FOUND);
        }

        if (exception.getMessage().toLowerCase().contains("already exists") || exception.getMessage().startsWith("Analyse codes and pinfl can not be null or empty") || exception.getMessage().startsWith("Unsupported Bearer Token")) {
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.BAD_REQUEST, 400, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
        }

        if (exception.getMessage().startsWith("Service unavailable")) {
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.SERVICE_UNAVAILABLE, 503, exception.getMessage(), null), HttpStatus.SERVICE_UNAVAILABLE);
        }

        if (exception.getMessage().equals("Access is denied") || exception.getMessage().equals("Доступ запрещен")) {
            log.info("##########################USER HAS NOT PERMISSION#############################" + exception.getMessage());
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.EXPECTATION_FAILED, 417, exception.getMessage(), null), HttpStatus.EXPECTATION_FAILED);
        } else
            return new ResponseEntity<>(new ResponseDto<>(ResponseDto.States.ERROR, HttpStatus.INTERNAL_SERVER_ERROR, 500, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
