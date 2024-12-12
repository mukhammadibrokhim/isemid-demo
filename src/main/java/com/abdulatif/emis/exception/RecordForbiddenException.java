package com.abdulatif.emis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class RecordForbiddenException extends RuntimeException {
    public RecordForbiddenException(String exception) {
        super(exception);
    }
}
