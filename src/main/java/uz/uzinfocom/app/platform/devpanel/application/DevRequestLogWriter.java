package uz.uzinfocom.app.platform.devpanel.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devpanel.domain.DevRequestLog;
import uz.uzinfocom.app.platform.devpanel.repository.DevRequestLogRepository;

import java.time.Instant;

/**
 * Persists one row per HTTP request, called from
 * {@code RequestLoggingFilter.logOnce(...)} with fields it has already
 * extracted - no duplicate parsing here. Runs on the app's existing
 * {@code applicationTaskExecutor} so this write never adds latency to the
 * request being tracked; if the executor is saturated, only this audit row
 * is dropped (logged), never the original request/response.
 *
 * <p>Unlike {@link DevErrorLogWriter} (failures only), this fires for every
 * request {@code RequestLoggingFilter} doesn't exclude, regardless of the
 * text-log verbosity settings - it's the source for "which resources did
 * this user use" (see {@code GET /v1/dev/requests?principal=...}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevRequestLogWriter {

    private final DevRequestLogRepository devRequestLogRepository;

    @Async("applicationTaskExecutor")
    @Transactional
    public void record(
            String traceId,
            String method,
            String route,
            String path,
            String query,
            int httpStatus,
            String outcome,
            long durationMs,
            String clientIp,
            String principal,
            String organizationId,
            String requestContentType,
            String responseContentType,
            long requestContentLength,
            String errorCode,
            String exceptionType,
            String rootCauseType,
            String message,
            String userAgent
    ) {
        try {
            DevRequestLog entry = DevRequestLog.builder()
                    .traceId(traceId)
                    .method(method)
                    .route(route)
                    .path(path)
                    .query(query)
                    .httpStatus(httpStatus)
                    .outcome(outcome)
                    .durationMs(durationMs)
                    .clientIp(clientIp)
                    .principal(principal)
                    .organizationId(organizationId)
                    .requestContentType(requestContentType)
                    .responseContentType(responseContentType)
                    .requestContentLength(requestContentLength)
                    .errorCode(errorCode)
                    .exceptionType(exceptionType)
                    .rootCauseType(rootCauseType)
                    .message(message)
                    .userAgent(userAgent)
                    .occurredAt(Instant.now())
                    .build();

            devRequestLogRepository.save(entry);
        } catch (RuntimeException persistenceFailure) {
            log.warn("event=dev_request_log_write_failure traceId={} path={} failureType={}",
                    traceId, path, persistenceFailure.getClass().getName());
        }
    }
}
