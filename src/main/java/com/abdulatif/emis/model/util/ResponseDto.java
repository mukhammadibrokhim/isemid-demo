package com.abdulatif.emis.model.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseDto<T> {

    public enum States {SUCCESS, ERROR, INFO, WARNING, EXPIRED_TEST_DATE, CONFLICT}

    private States state;

    private HttpStatus status;

    private Integer statusCode;

    private String message;

    private T data;

    public ResponseDto(States state, HttpStatus status, Integer statusCode, String message, T data) {
        if (state != null) {
            setState(state);
        }
        if (status != null) {
            setStatus(status);
        }
        if (statusCode != null) {
            setStatusCode(statusCode);
        }
        if (message != null) {
            setMessage(message);
        }
        if (data != null) {
            setData(data);
        }
    }
}
