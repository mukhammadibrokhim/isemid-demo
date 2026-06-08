package uz.uzinfocom.app.integration.api2.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uz.uzinfocom.app.integration.api2.api.dto.Api2ErrorResponse;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenLookupValidationException;
import uz.uzinfocom.app.integration.api2.citizen.exception.UnsupportedCitizenLookupTypeException;
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.legalentity.exception.LegalEntityValidationException;
import uz.uzinfocom.app.platform.observability.TraceContext;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "uz.uzinfocom.app.integration.api2")
public class Api2ExceptionHandler {

    @ExceptionHandler(Api2Exception.class)
    public ResponseEntity<Api2ErrorResponse> handleApi2Exception(
            Api2Exception exception,
            HttpServletRequest request
    ) {
        List<FieldValidationError> fieldErrors = fieldErrors(exception);
        String traceId = traceId();

        log.warn(
                "API2 exception handled. traceId={}, exception={}, errorCode={}, operation={}, method={}, path={}, fields={}",
                traceId,
                exception.getClass().getSimpleName(),
                exception.getErrorCode(),
                exception.getOperation(),
                request.getMethod(),
                request.getRequestURI(),
                fieldErrors.stream().map(FieldValidationError::field).toList()
        );

        return ResponseEntity
                .status(exception.getResponseStatus())
                .body(error(
                        exception.getResponseStatus(),
                        exception.getErrorCode(),
                        exception.getMessage(),
                        exception.getOperation(),
                        exception.getUpstreamStatus(),
                        exception.getUpstreamCode(),
                        exception.getUpstreamMessage(),
                        exception.getUpstreamDetail(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Api2ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String errorCode = isLegalEntityRequest(request)
                ? "LEGAL_ENTITY_VALIDATION_FAILED"
                : "CITIZEN_LOOKUP_VALIDATION_FAILED";
        String message = isLegalEntityRequest(request)
                ? "Legal entity lookup request is invalid."
                : "Citizen lookup request is invalid.";
        String operation = isLegalEntityRequest(request)
                ? "LEGAL_ENTITY_TIN_LOOKUP"
                : "CITIZEN_LOOKUP";
        List<FieldValidationError> fieldErrors = List.of(new FieldValidationError(
                exception.getParameterName(),
                exception.getParameterName() + " is required."
        ));

        log.warn(
                "API2 request parameter missing. traceId={}, errorCode={}, method={}, path={}, parameter={}",
                traceId(),
                errorCode,
                request.getMethod(),
                request.getRequestURI(),
                exception.getParameterName()
        );

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST,
                        errorCode,
                        message,
                        operation,
                        null,
                        null,
                        null,
                        null,
                        fieldErrors
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Api2ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String errorCode = "CITIZEN_LOOKUP_VALIDATION_FAILED";
        String message = "Citizen lookup request is invalid.";
        String operation = "CITIZEN_LOOKUP";
        String field = exception.getName();
        String fieldMessage = field + " has an invalid value.";

        if ("type".equals(field) && CitizenLookupType.class.equals(exception.getRequiredType())) {
            errorCode = "CITIZEN_LOOKUP_TYPE_UNSUPPORTED";
            message = "Unsupported citizen lookup type.";
            fieldMessage = "type must be one of NNUZB, PPN, CZ.";
        } else if ("birth_date".equals(field)) {
            fieldMessage = "birth_date must be an ISO date.";
        }

        List<FieldValidationError> fieldErrors = List.of(new FieldValidationError(field, fieldMessage));

        log.warn(
                "API2 request argument type mismatch. traceId={}, errorCode={}, method={}, path={}, field={}",
                traceId(),
                errorCode,
                request.getMethod(),
                request.getRequestURI(),
                field
        );

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST,
                        errorCode,
                        message,
                        operation,
                        null,
                        null,
                        null,
                        null,
                        fieldErrors
                ));
    }

    private Api2ErrorResponse error(
            HttpStatus status,
            String errorCode,
            String message,
            String operation,
            Integer upstreamStatus,
            String upstreamCode,
            String upstreamMessage,
            String upstreamDetail,
            List<FieldValidationError> fieldErrors
    ) {
        return new Api2ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                errorCode,
                message,
                operation,
                upstreamStatus,
                upstreamCode,
                upstreamMessage,
                upstreamDetail,
                traceId(),
                fieldErrors == null ? List.of() : fieldErrors
        );
    }

    private List<FieldValidationError> fieldErrors(Api2Exception exception) {
        if (exception instanceof CitizenLookupValidationException validationException) {
            return validationException.fieldErrors();
        }

        if (exception instanceof UnsupportedCitizenLookupTypeException unsupportedTypeException) {
            return unsupportedTypeException.fieldErrors();
        }

        if (exception instanceof LegalEntityValidationException validationException) {
            return validationException.fieldErrors();
        }

        return List.of();
    }

    private boolean isLegalEntityRequest(HttpServletRequest request) {
        return request.getRequestURI() != null
                && request.getRequestURI().contains("/v1/legal-entity");
    }

    private String traceId() {
        return MDC.get(TraceContext.MDC_KEY);
    }
}
