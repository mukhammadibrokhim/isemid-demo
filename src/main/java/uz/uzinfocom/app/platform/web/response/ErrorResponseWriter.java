package uz.uzinfocom.app.platform.web.response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.shared.exception.ErrorCode;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.observability.ObservabilityProperties;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.FieldViolationResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final JsonMapper objectMapper;
    private final MessageResolver messages;
    private final TraceIdProvider traceIdProvider;
    private final ObservabilityProperties observabilityProperties;

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode
    ) throws IOException {
        write(
                request,
                response,
                status,
                errorCode.getCode(),
                messages.resolve(errorCode.getDefaultMessageCode()),
                List.of()
        );
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode,
            String messageCode,
            Object... args
    ) throws IOException {
        write(
                request,
                response,
                status,
                errorCode.getCode(),
                messages.resolve(messageCode, args),
                List.of()
        );
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            List<FieldViolationResponse> violations
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        String traceId = traceIdProvider.getOrCreate(request);

        ErrorResponse body = ErrorResponse.of(
                code,
                message,
                traceId,
                request.getRequestURI(),
                violations == null ? List.of() : violations
        );

        response.resetBuffer();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(observabilityProperties.getTraceIdHeader(), traceId);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
