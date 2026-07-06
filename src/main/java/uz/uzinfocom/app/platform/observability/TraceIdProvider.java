package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class TraceIdProvider {

    private static final int TRACE_ID_BYTES = 16;

    private final ObservabilityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String resolveIncomingTraceId(String candidate) {
        if (properties.isAcceptIncomingTraceId() && isValid(candidate)) {
            return candidate;
        }
        return generateTraceId();
    }

    public boolean isValid(String candidate) {
        if (!StringUtils.hasText(candidate)
                || candidate.length() > properties.getTraceIdMaxLength()) {
            return false;
        }

        for (int index = 0; index < candidate.length(); index++) {
            char character = candidate.charAt(index);
            boolean alphanumeric = (character >= 'a' && character <= 'z')
                    || (character >= 'A' && character <= 'Z')
                    || (character >= '0' && character <= '9');
            boolean safeSeparator = character == '-'
                    || character == '_'
                    || character == '.'
                    || character == ':';

            if (!alphanumeric && !safeSeparator) {
                return false;
            }
        }
        return true;
    }

    public String generateTraceId() {
        byte[] bytes = new byte[TRACE_ID_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public String currentTraceId() {
        return TraceContext.currentTraceId();
    }

    public String getTraceId(HttpServletRequest request) {
        String traceId = TraceContext.getTraceId(request);
        return StringUtils.hasText(traceId) ? traceId : "N/A";
    }
}
