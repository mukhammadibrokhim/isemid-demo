package uz.uzinfocom.app.orchestration.webhook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.Instant;

/**
 * One outbound status-change notification attempt (and its retry history) for
 * a single {@code IntegrationClient}'s webhook callback. Enqueued by
 * {@code OutboundWebhookEventListener}, one row per {@code StatusChangedEvent}
 * — never sent synchronously; {@code OutboundWebhookDispatchScheduler} is the
 * only path that actually attempts delivery, so first attempts and retries
 * share the same send path.
 * <p>
 * ddl-auto=validate — the DB schema is owned by Liquibase
 * (db.migration/changelog/webhook/), see {@code ExportJob} for the same
 * convention.
 */
@Getter
@Setter
@Entity
@Table(
        name = "outbound_webhook_dispatch",
        indexes = {
                @Index(name = "idx_outbound_webhook_dispatch_status_next_attempt", columnList = "status,next_attempt_at"),
                @Index(name = "idx_outbound_webhook_dispatch_integration_client_id", columnList = "integration_client_id"),
                @Index(name = "idx_outbound_webhook_dispatch_entity", columnList = "entity_type,entity_id")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OutboundWebhookDispatch extends AuditableEntity {

    @Column(name = "integration_client_id", nullable = false)
    private Long integrationClientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 32)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "old_status", length = 32)
    private String oldStatus;

    @Column(name = "new_status", nullable = false, length = 32)
    private String newStatus;

    @Column(name = "payload", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboundWebhookDispatchStatus status;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_attempted_at")
    private Instant lastAttemptedAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "last_http_status")
    private Integer lastHttpStatus;

    public void markSending() {
        this.status = OutboundWebhookDispatchStatus.SENDING;
        this.attemptCount = this.attemptCount + 1;
        this.lastAttemptedAt = Instant.now();
    }

    public void recordSuccess(int httpStatus) {
        this.status = OutboundWebhookDispatchStatus.SUCCEEDED;
        this.lastHttpStatus = httpStatus;
        this.lastError = null;
    }

    public void recordFailureAndReschedule(String error, Integer httpStatus, Instant nextAttemptAt) {
        this.status = OutboundWebhookDispatchStatus.PENDING;
        this.lastError = truncate(error);
        this.lastHttpStatus = httpStatus;
        this.nextAttemptAt = nextAttemptAt;
    }

    public void markExhausted(String error, Integer httpStatus) {
        this.status = OutboundWebhookDispatchStatus.EXHAUSTED;
        this.lastError = truncate(error);
        this.lastHttpStatus = httpStatus;
    }

    /**
     * Resets a dispatch back to the front of the queue for an immediate
     * manual retry from the dev panel, regardless of how it got here
     * (still-retrying {@code PENDING} or already {@code EXHAUSTED}).
     */
    public void retryNow() {
        this.status = OutboundWebhookDispatchStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = Instant.now();
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
    }
}
