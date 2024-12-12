package com.abdulatif.emis.model.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date timestamp;
    private String message;
    private String error;
    private List<String> details;
    private int status;

    public ErrorResponse(Date timestamp, String message, String error, int status) {
        this.timestamp = timestamp;
        this.message = message;
        this.error = error;
        this.status = status;
    }

    public ErrorResponse(Date timestamp, String message, HttpStatus httpStatus) {
        this.timestamp = timestamp;
        this.message = message;
        this.error = httpStatus.getReasonPhrase();
        this.status = httpStatus.value();
    }

    public ErrorResponse(Date timestamp, String message, HttpStatus httpStatus, List<String> details) {
        this.timestamp = timestamp;
        this.message = message;
        this.error = httpStatus.getReasonPhrase();
        this.status = httpStatus.value();
        this.details = details;
    }

    public ErrorResponse(String message, List<String> details) {
        super();
        this.message = message;
        this.details = details;
    }
}
