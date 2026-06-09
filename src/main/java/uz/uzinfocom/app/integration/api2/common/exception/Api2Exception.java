package uz.uzinfocom.app.integration.api2.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

import java.util.List;

@Getter
public class Api2Exception extends RuntimeException {

    private final HttpStatus responseStatus;
    private final String errorCode;
    private final String messageCode;
    private final String operation;
    private final Integer upstreamStatus;
    private final String upstreamCode;
    private final String upstreamMessage;
    private final String upstreamDetail;
    private final List<FieldValidationError> fieldErrors;

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String messageCode,
            String operation,
            Api2UpstreamError upstreamError
    ) {
        this(responseStatus, errorCode, messageCode, operation, upstreamError, List.of(), null);
    }

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String messageCode,
            String operation,
            Api2UpstreamError upstreamError,
            List<FieldValidationError> fieldErrors
    ) {
        this(responseStatus, errorCode, messageCode, operation, upstreamError, fieldErrors, null);
    }

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String messageCode,
            String operation,
            Api2UpstreamError upstreamError,
            Throwable cause
    ) {
        this(responseStatus, errorCode, messageCode, operation, upstreamError, List.of(), cause);
    }

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String messageCode,
            String operation,
            Api2UpstreamError upstreamError,
            List<FieldValidationError> fieldErrors,
            Throwable cause
    ) {
        super(messageCode, cause);
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
        this.messageCode = messageCode;
        this.operation = operation;
        this.upstreamStatus = upstreamError == null ? null : upstreamError.status();
        this.upstreamCode = upstreamError == null ? null : upstreamError.code();
        this.upstreamMessage = upstreamError == null ? null : upstreamError.message();
        this.upstreamDetail = upstreamError == null ? null : upstreamError.detail();
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

}
