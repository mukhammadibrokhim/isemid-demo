package com.abdulatif.emis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DeleteException extends RuntimeException {

    public DeleteException(String exception) {
        super(exception);
    }

    public DeleteException() {
        super();
    }

    public DeleteException(String message, Throwable cause,
                           boolean enableSuppression,
                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}