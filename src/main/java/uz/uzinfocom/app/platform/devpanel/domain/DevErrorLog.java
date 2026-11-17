package uz.uzinfocom.app.platform.devpanel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.time.Instant;

/**
 * One row per failed (HTTP status &gt;= 400) request, captured from
 * {@code RequestLoggingFilter} - the same fields it already logs to the
 * "HTTP_REQUEST" SLF4J logger, persisted here so they're queryable via
 * {@code GET /v1/dev/errors} instead of grep'd out of console/file logs.
 *
 * <p>Deliberately NOT an {@link uz.uzinfocom.app.platform.persistence.entity.AuditableEntity}:
 * {@code SecurityAuditorAware.currentUserId()} only recognizes
 * {@code FederatedAuthenticationToken} (SSO/DHP users), so {@code @CreatedBy} would
 * silently stay null for every row here (written by the request-logging filter, not
 * a human action) and for any {@code DevUser}-authenticated resolve action.
 * {@link #resolvedBy} is set explicitly instead.
 *
 * <p>One row per occurrence (no dedup/counter) - matches how the request-logging
 * filter already logs one line per event, and keeps OPEN/RESOLVED meaningful per
 * incident rather than per error-type.
 */
@Getter
@Setter
@Entity
@Table(name = "dev_error_log")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DevErrorLog extends BaseEntity {

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "exception_type")
    private String exceptionType;

    @Column(length = 500)
    private String path;

    @Column(length = 16)
    private String method;

    @Column
    private String principal;

    @Column(length = 500)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevErrorStatus status = DevErrorStatus.OPEN;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
