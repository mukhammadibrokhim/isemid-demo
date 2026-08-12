package uz.uzinfocom.app.platform.devpanel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.time.Instant;

/**
 * One row per HTTP request handled by the app - not just failures, see
 * {@link DevErrorLog} for those. Captured from {@code RequestLoggingFilter}
 * independent of its text-log verbosity settings
 * ({@code app.observability.http-logging.*}), so "which resources did this
 * user use" (see {@code GET /v1/dev/requests?principal=...}) stays complete
 * regardless of how the console/file HTTP_REQUEST logger is configured.
 *
 * <p>Deliberately NOT an {@link uz.uzinfocom.app.platform.persistence.entity.AuditableEntity}
 * - same reasoning as {@link DevLoginHistory}: this row is itself the record
 * of "who called what," written by the request-logging filter rather than a
 * human action, and {@code SecurityAuditorAware} wouldn't recognize
 * {@code DevUser}/pre-authentication callers anyway.
 */
@Getter
@Setter
@Entity
@Table(name = "dev_request_log")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DevRequestLog extends BaseEntity {

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(length = 16)
    private String method;

    @Column(length = 500)
    private String route;

    @Column(length = 500)
    private String path;

    @Column(length = 500)
    private String query;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(length = 32)
    private String outcome;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column
    private String principal;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "request_content_type", length = 100)
    private String requestContentType;

    @Column(name = "response_content_type", length = 100)
    private String responseContentType;

    @Column(name = "request_content_length")
    private Long requestContentLength;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "exception_type")
    private String exceptionType;

    @Column(name = "root_cause_type")
    private String rootCauseType;

    @Column(length = 500)
    private String message;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
