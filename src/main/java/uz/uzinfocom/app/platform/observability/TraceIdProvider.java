package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TraceIdProvider {

    public String getTraceId(HttpServletRequest request) {
        Object requestTraceId = request.getAttribute(TraceContext.REQUEST_ATTRIBUTE);

        if (requestTraceId instanceof String traceId && StringUtils.hasText(traceId)) {
            return traceId;
        }

        String mdcTraceId = MDC.get(TraceContext.MDC_KEY);

        if (StringUtils.hasText(mdcTraceId)) {
            return mdcTraceId;
        }

        return "N/A";
    }
}
