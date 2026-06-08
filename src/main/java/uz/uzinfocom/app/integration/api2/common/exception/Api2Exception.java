package uz.uzinfocom.app.integration.api2.common.exception;

import org.springframework.http.HttpStatus;
import uz.uzinfocom.app.integration.api2.common.support.Api2UpstreamError;

public class Api2Exception extends RuntimeException {

    private final HttpStatus responseStatus;
    private final String errorCode;
    private final String operation;
    private final Integer upstreamStatus;
    private final String upstreamCode;
    private final String upstreamMessage;
    private final String upstreamDetail;

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String message,
            String operation,
            Api2UpstreamError upstreamError
    ) {
        this(responseStatus, errorCode, message, operation, upstreamError, null);
    }

    public Api2Exception(
            HttpStatus responseStatus,
            String errorCode,
            String message,
            String operation,
            Api2UpstreamError upstreamError,
            Throwable cause
    ) {
        super(message, cause);
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
        this.operation = operation;
        this.upstreamStatus = upstreamError == null ? null : upstreamError.status();
        this.upstreamCode = upstreamError == null ? null : upstreamError.code();
        this.upstreamMessage = upstreamError == null ? null : upstreamError.message();
        this.upstreamDetail = upstreamError == null ? null : upstreamError.detail();
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getUpstreamStatus() {
        return upstreamStatus;
    }

    public String getUpstreamCode() {
        return upstreamCode;
    }

    public String getUpstreamMessage() {
        return upstreamMessage;
    }

    public String getUpstreamDetail() {
        return upstreamDetail;
    }
}
