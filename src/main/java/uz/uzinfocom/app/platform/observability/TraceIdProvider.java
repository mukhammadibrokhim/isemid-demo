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

    public String resolveOrCreate(HttpServletRequest request, String incomingCandidate) {
        String requestTraceId = TraceContext.getRequestTraceId(request);
        if (requestTraceId != null) {
            return requestTraceId;
        }

        String incomingTraceId = normalizeValidIncoming(incomingCandidate);
        if (incomingTraceId != null) {
            TraceContext.setTraceId(request, incomingTraceId);
            return incomingTraceId;
        }

        if (request != null
                && request.getDispatcherType() == jakarta.servlet.DispatcherType.REQUEST) {
            String generatedTraceId = generateTraceId();
            TraceContext.setTraceId(request, generatedTraceId);
            return generatedTraceId;
        }

        String currentTraceId = currentTraceId();
        if (StringUtils.hasText(currentTraceId)) {
            TraceContext.setTraceId(request, currentTraceId);
            return currentTraceId;
        }

        String generatedTraceId = generateTraceId();
        TraceContext.setTraceId(request, generatedTraceId);
        return generatedTraceId;
    }

    public String getOrCreate(HttpServletRequest request) {
        String requestTraceId = TraceContext.getRequestTraceId(request);
        if (requestTraceId != null) {
            return requestTraceId;
        }

        String currentTraceId = currentTraceId();
        if (StringUtils.hasText(currentTraceId)) {
            TraceContext.setTraceId(request, currentTraceId);
            return currentTraceId;
        }

        String generatedTraceId = generateTraceId();
        TraceContext.setTraceId(request, generatedTraceId);
        return generatedTraceId;
    }

    public String resolveIncomingTraceId(String candidate) {
        String accepted = normalizeValidIncoming(candidate);
        return accepted != null ? accepted : generateTraceId();
    }

    public boolean isValid(String candidate) {
        return normalizeValidCandidate(candidate) != null;
    }

    public String normalize(String candidate) {
        return normalizeValidCandidate(candidate);
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
        return getOrCreate(request);
    }

    private String normalizeValidIncoming(String candidate) {
        return properties.isAcceptIncomingTraceId() ? normalizeValidCandidate(candidate) : null;
    }

    private String normalizeValidCandidate(String candidate) {
        if (candidate == null) {
            return null;
        }

        String normalized = candidate.trim();
        if (normalized.length() < properties.getTraceIdMinLength()
                || normalized.length() > properties.getTraceIdMaxLength()) {
            return null;
        }

        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            boolean alphanumeric = (character >= 'a' && character <= 'z')
                    || (character >= 'A' && character <= 'Z')
                    || (character >= '0' && character <= '9');
            boolean safeSeparator = properties.getTraceIdAllowedSeparators().indexOf(character) >= 0;
            if (!alphanumeric && !safeSeparator) {
                return null;
            }
        }
        return normalized;
    }
}
