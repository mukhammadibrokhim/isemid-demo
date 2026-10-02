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
import java.util.UUID;

/**
 * One row per login attempt (success or failure) against {@code /v1/auth/login/{provider}}
 * - see {@code LoginHistoryRecorder}. Deliberately NOT an
 * {@link uz.uzinfocom.app.platform.persistence.entity.AuditableEntity}: this row IS the
 * audit record of "who logged in," there's no separate human "creator" action to attribute
 * it to, and {@code SecurityAuditorAware} wouldn't recognize the pre-authentication caller
 * anyway.
 */
@Getter
@Setter
@Entity
@Table(name = "dev_login_history")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DevLoginHistory extends BaseEntity {

    @Column(length = 50)
    private String provider;

    @Column
    private String username;

    /**
     * The external identity's practitioner UUID (see {@code ExternalIdentityPayload#practitionerUuid}),
     * resolved from the issued access token same as {@link #username}. Null whenever
     * {@link #username} is null (failed attempts, or resolution failure).
     */
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
