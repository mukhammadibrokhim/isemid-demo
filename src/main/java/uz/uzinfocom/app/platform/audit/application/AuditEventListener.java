package uz.uzinfocom.app.platform.audit.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.uzinfocom.app.platform.audit.domain.AuditEvent;
import uz.uzinfocom.app.platform.audit.domain.AuditEventType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.FieldsChangedEvent;
import uz.uzinfocom.app.platform.audit.event.OrganizationReassignedEvent;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.audit.repository.AuditEventRepository;

/**
 * Writes {@code audit_event} rows outside the originating business transaction — each
 * handler only fires {@link TransactionPhase#AFTER_COMMIT}, on the shared async executor
 * (see {@code LoginHistoryRecorder} for the same pattern), so a slow or failing audit
 * write can never delay a response or roll back the Form058/Form0581/Act change that
 * triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditEventRepository auditEventRepository;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(EntityCreatedEvent event) {
        safeSave(AuditEvent.builder()
                .eventType(AuditEventType.CREATED)
                .entityType(event.entityType())
                .entityId(event.entityId())
                .actorUserId(event.actorUserId())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StatusChangedEvent event) {
        safeSave(AuditEvent.builder()
                .eventType(AuditEventType.STATUS_CHANGED)
                .entityType(event.entityType())
                .entityId(event.entityId())
                .oldStatus(event.oldStatus())
                .newStatus(event.newStatus())
                .actorUserId(event.actorUserId())
                .reason(event.reason())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrganizationReassignedEvent event) {
        safeSave(AuditEvent.builder()
                .eventType(AuditEventType.ORG_REASSIGNED)
                .entityType(event.entityType())
                .entityId(event.entityId())
                .oldOrgId(event.oldOrgId())
                .newOrgId(event.newOrgId())
                .actorUserId(event.actorUserId())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(FieldsChangedEvent event) {
        if (event.changes().isEmpty()) {
            return;
        }
        safeSave(AuditEvent.builder()
                .eventType(AuditEventType.FIELD_CHANGED)
                .entityType(event.entityType())
                .entityId(event.entityId())
                .changes(event.changes())
                .actorUserId(event.actorUserId())
                .occurredAt(event.occurredAt())
                .build());
    }

    private void safeSave(AuditEvent auditEvent) {
        try {
            auditEventRepository.save(auditEvent);
        } catch (RuntimeException persistenceFailure) {
            log.error("event=audit_event_write_failure entityType={} entityId={} eventType={} failureType={}",
                    auditEvent.getEntityType(), auditEvent.getEntityId(), auditEvent.getEventType(),
                    persistenceFailure.getClass().getName(), persistenceFailure);
        }
    }
}
