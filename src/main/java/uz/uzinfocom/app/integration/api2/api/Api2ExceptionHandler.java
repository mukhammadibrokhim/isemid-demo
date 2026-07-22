package uz.uzinfocom.app.integration.api2.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import uz.uzinfocom.app.integration.api2.common.exception.Api2Exception;
import uz.uzinfocom.app.integration.api2.common.support.Api2ResponseBodySanitizer;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.time.OffsetDateTime;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "uz.uzinfocom.app.integration.api2")
@RequiredArgsConstructor
public class Api2ExceptionHandler {

    private final MessageResolver messages;
    private final TraceIdProvider traceIdProvider;

    @ExceptionHandler(Api2Exception.class)
    public ResponseEntity<Api2ErrorResponse> handleApi2Exception(
            Api2Exception exception,
            HttpServletRequest request
    ) {
        String traceId = traceIdProvider.getOrCreate(request);
        String message = messages.resolve(exception.getMessageCode());
        List<FieldValidationError> fieldErrors = localizedFieldErrors(exception.getFieldErrors());

        RequestLogErrorContext.attach(
                request,
                exception.getErrorCode(),
                exception.getClass().getSimpleName() + " during " + exception.getOperation(),
                exception
        );

        return ResponseEntity
                .status(exception.getResponseStatus())
                .body(error(
                        exception.getResponseStatus(),
                        exception.getErrorCode(),
                        message,
                        exception.getOperation(),
                        exception.getUpstreamStatus(),
                        safeUpstreamText(exception.getUpstreamCode()),
                        safeUpstreamText(exception.getUpstreamMessage()),
                        safeUpstreamText(exception.getUpstreamDetail()),
                        traceId,
                        fieldErrors
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Api2ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        Api2RequestContext context = requestContext(request);
        String traceId = traceIdProvider.getOrCreate(request);
        List<FieldValidationError> fieldErrors = localizedFieldErrors(List.of(new FieldValidationError(
                exception.getParameterName(),
                "validation.required"
        )));

        RequestLogErrorContext.attach(
                request,
                context.validationErrorCode(),
                "Required request parameter is missing: " + exception.getParameterName(),
                exception
        );

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST,
                        context.validationErrorCode(),
                        messages.resolve(context.validationMessageCode()),
                        context.operation(),
                        null,
                        null,
                        null,
                        null,
                        traceId,
                        fieldErrors
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Api2ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        Api2RequestContext context = requestContext(request);
        String traceId = traceIdProvider.getOrCreate(request);
        String errorCode = context.validationErrorCode();
        String messageCode = context.validationMessageCode();
        String field = exception.getName();
        String fieldMessageCode = "validation.invalid_value";

        if (!context.legalEntity()
                && "type".equals(field)
                && CitizenLookupType.class.equals(exception.getRequiredType())) {
            errorCode = "CITIZEN_LOOKUP_TYPE_UNSUPPORTED";
            messageCode = "api2.citizen.error.unsupported_type";
            fieldMessageCode = "validation.citizen_type.allowed";
        } else if ("birth_date".equals(field)) {
            fieldMessageCode = "validation.birth_date.invalid";
        }

        List<FieldValidationError> fieldErrors = localizedFieldErrors(List.of(new FieldValidationError(
                field,
                fieldMessageCode
        )));

        RequestLogErrorContext.attach(
                request,
                errorCode,
                "Request argument type mismatch: " + field,
                exception
        );

        return ResponseEntity
                .badRequest()
                .body(error(
                        HttpStatus.BAD_REQUEST,
                        errorCode,
                        messages.resolve(messageCode),
                        context.operation(),
                        null,
                        null,
                        null,
                        null,
                        traceId,
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
            String traceId,
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
                traceId,
                fieldErrors == null ? List.of() : fieldErrors
        );
    }

    private List<FieldValidationError> localizedFieldErrors(List<FieldValidationError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return List.of();
        }

        return fieldErrors.stream()
                .limit(100)
                .map(error -> new FieldValidationError(
                        error.field(),
                        messages.resolve(error.message())
                ))
                .toList();
    }

    private String safeUpstreamText(String value) {
        return Api2ResponseBodySanitizer.sanitize(value, null);
    }

    private Api2RequestContext requestContext(HttpServletRequest request) {
        boolean legalEntity = isLegalEntityRequest(request);

        if (legalEntity) {
            return new Api2RequestContext(
                    true,
                    "LEGAL_ENTITY_VALIDATION_FAILED",
                    "api2.legal_entity.error.validation",
                    "LEGAL_ENTITY_TIN_LOOKUP"
            );
        }

        return new Api2RequestContext(
                false,
                "CITIZEN_LOOKUP_VALIDATION_FAILED",
                "api2.citizen.error.validation",
                "CITIZEN_LOOKUP"
        );
    }

    private boolean isLegalEntityRequest(HttpServletRequest request) {
        return request.getRequestURI() != null
                && request.getRequestURI().startsWith(ApiPaths.LegalEntity.ROOT);
    }

    private record Api2RequestContext(
            boolean legalEntity,
            String validationErrorCode,
            String validationMessageCode,
            String operation
    ) {
    }
}
