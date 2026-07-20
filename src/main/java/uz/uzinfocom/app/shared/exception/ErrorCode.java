package uz.uzinfocom.app.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "error.unauthorized"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "error.forbidden"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "error.validation_failed"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "error.not_found"),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "error.conflict"),
    SECURITY_VIOLATION("SECURITY_VIOLATION", HttpStatus.FORBIDDEN, "error.forbidden"),
    SCOPE_VIOLATION("SCOPE_VIOLATION", HttpStatus.FORBIDDEN, "organization.scope_violation"),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "error.internal_server"),
    DATA_INTEGRITY("DATA_INTEGRITY", HttpStatus.INTERNAL_SERVER_ERROR, "error.data_integrity"),
    ASYNC_EXECUTOR_SATURATED("ASYNC_EXECUTOR_SATURATED", HttpStatus.SERVICE_UNAVAILABLE, "error.service_unavailable"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "error.endpoint_not_found"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "error.method_not_supported"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "error.unsupported_media_type"),
    NOT_ACCEPTABLE("NOT_ACCEPTABLE", HttpStatus.NOT_ACCEPTABLE, "error.not_acceptable"),

    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "error.bad_request"),
    REQUEST_BODY_MISSING("REQUEST_BODY_MISSING", HttpStatus.BAD_REQUEST, "error.request_body_missing"),
    MALFORMED_JSON("MALFORMED_JSON", HttpStatus.BAD_REQUEST, "error.malformed_json");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessageCode;
}
