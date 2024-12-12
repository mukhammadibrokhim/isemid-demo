package com.abdulatif.emis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecordBadRequestException extends RuntimeException {

    public RecordBadRequestException(String exception) {
        super(exception);
    }
}
