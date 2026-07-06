package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TraceContext {

    public static final String MDC_KEY = "traceId";

    public static final String REQUEST_ATTRIBUTE =
            TraceContext.class.getName() + ".TRACE_ID";

    public static String currentTraceId() {
        return MDC.get(MDC_KEY);
    }

    public static String getRequestTraceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(REQUEST_ATTRIBUTE);
        return attribute instanceof String traceId && StringUtils.hasText(traceId)
                ? traceId
                : null;
    }

    public static String getTraceId(HttpServletRequest request) {
        String requestTraceId = getRequestTraceId(request);
        if (requestTraceId != null) {
            return requestTraceId;
        }
        return currentTraceId();
    }

    public static void setTraceId(
            HttpServletRequest request,
            String traceId
    ) {
        if (request == null || !StringUtils.hasText(traceId)) {
            return;
        }

        request.setAttribute(REQUEST_ATTRIBUTE, traceId);
    }

    public static Scope open(String traceId) {
        return new Scope(traceId);
    }

    public static void run(
            String traceId,
            Runnable action
    ) {
        try (Scope ignored = open(traceId)) {
            action.run();
        }
    }

    public static final class Scope implements AutoCloseable {

        private final String previousTraceId;
        private boolean closed;

        private Scope(String traceId) {
            this.previousTraceId = MDC.get(MDC_KEY);

            if (StringUtils.hasText(traceId)) {
                MDC.put(MDC_KEY, traceId);
            } else {
                MDC.remove(MDC_KEY);
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }

            closed = true;

            if (StringUtils.hasText(previousTraceId)) {
                MDC.put(MDC_KEY, previousTraceId);
            } else {
                MDC.remove(MDC_KEY);
            }
        }
    }
}
