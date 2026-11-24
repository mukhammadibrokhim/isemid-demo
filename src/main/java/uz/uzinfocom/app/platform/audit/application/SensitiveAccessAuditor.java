package uz.uzinfocom.app.platform.audit.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditEvent;
import uz.uzinfocom.app.platform.audit.domain.AuditEventType;
import uz.uzinfocom.app.platform.audit.repository.AuditEventRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.time.Instant;
import java.util.Map;

/**
 * Records "who looked at a sensitive value" rows. Unlike {@link AuditEventListener} (async, after
 * commit, failures swallowed) this write is synchronous and in its own transaction that commits
 * before returning: if the row cannot be saved the exception propagates and the caller never
 * hands the value out — no audit trail, no reveal. Never put the revealed value itself in
 * {@code details}.
 */
@Component
@RequiredArgsConstructor
public class SensitiveAccessAuditor {

    private final AuditEventRepository auditEventRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordReveal(AuditEntityType entityType, Long entityId, Map<String, Object> details) {
        auditEventRepository.save(AuditEvent.builder()
                .eventType(AuditEventType.SENSITIVE_DATA_REVEALED)
                .entityType(entityType)
                .entityId(entityId)
                .actorUserId(currentUserProvider.userIdOrNull())
                .changes(details)
                .occurredAt(Instant.now())
                .build());
    }
}
