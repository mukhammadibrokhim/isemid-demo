package uz.uzinfocom.app.platform.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.export.domain.event.ExportJobCompletedEvent;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.notification.domain.Notification;
import uz.uzinfocom.app.platform.notification.domain.NotificationType;
import uz.uzinfocom.app.platform.notification.repository.NotificationRepository;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Fans out {@link Notification} rows for the same {@code AFTER_COMMIT} business events
 * {@code AuditEventListener} already consumes — Spring dispatches an event to every matching
 * {@code @TransactionalEventListener} bean, so this runs alongside the audit trail without any
 * change to the publishing command services (except {@code CardCommandService}, which didn't
 * publish a per-card creation event at all before this). {@link ExportJobCompletedEvent} is the
 * one trigger with its own dedicated event type rather than reusing
 * {@code EntityCreatedEvent}/{@code StatusChangedEvent} — export jobs aren't part of the audit
 * trail and always have exactly one recipient (see that event's own javadoc).
 *
 * <p>Each notification kind is individually toggleable via {@link SystemSettingResolver} keys
 * (see the {@code KEY_*} constants) — writable from either {@code /v1/admin/settings} or the
 * dev-monitoring panel's {@code /v1/dev/settings} (same {@code system_settings} table), with no
 * row required: an unset key defaults to enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String KEY_FORM058_RECEIVED_ENABLED = "notification.form058-received.enabled";
    private static final String KEY_FORM0581_RECEIVED_ENABLED = "notification.form0581-received.enabled";
    private static final String KEY_CARD_ASSIGNED_ENABLED = "notification.card-assigned.enabled";
    private static final String KEY_ACT_ASSIGNED_ENABLED = "notification.act-assigned.enabled";
    private static final String KEY_ACT_LIS_RESPONSE_ENABLED = "notification.act-lis-response.enabled";
    private static final String KEY_EXPORT_READY_ENABLED = "notification.export-ready.enabled";

    private final Form058JpaRepository form058Repository;
    private final Form0581JpaRepository form0581Repository;
    private final CardRepository cardRepository;
    private final ActRepository actRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SystemSettingResolver systemSettingResolver;
    private final JsonMapper objectMapper;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(EntityCreatedEvent event) {
        switch (event.entityType()) {
            case FORM058 -> handleForm058Received(event);
            case FORM0581 -> handleForm0581Received(event);
            case CARD -> handleCardAssigned(event);
            case ACT -> handleActAssigned(event);
        }
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StatusChangedEvent event) {
        if (event.entityType() != AuditEntityType.ACT) {
            return;
        }
        if (!"SENT".equals(event.oldStatus()) || !"COMPLETED".equals(event.newStatus())) {
            return;
        }
        handleActLisResponse(event);
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ExportJobCompletedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_EXPORT_READY_ENABLED, true)) {
            return;
        }
        if (event.recipientUserId() == null) {
            log.warn("event=notification_source_not_found entityType=EXPORT_JOB entityId={} reason=no_recipient",
                    event.jobId());
            return;
        }

        fanOut(NotificationType.EXPORT_READY, AuditEntityType.EXPORT_JOB, event.jobId(), null,
                List.of(event.recipientUserId()), "notification.export-ready",
                new Object[]{event.fileName()});
    }

    private void handleForm058Received(EntityCreatedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM058_RECEIVED_ENABLED, true)) {
            return;
        }
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }
        notifyReceiverOrganization(event, NotificationType.FORM058_RECEIVED, "notification.form058-received",
                form058.getReceiverOrganizationId());
    }

    private void handleForm0581Received(EntityCreatedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_RECEIVED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }
        notifyReceiverOrganization(event, NotificationType.FORM0581_RECEIVED, "notification.form0581-received",
                form0581.getReceiverOrganizationId());
    }

    private void notifyReceiverOrganization(
            EntityCreatedEvent event, NotificationType type, String messageKey, Long receiverOrganizationId
    ) {
        if (receiverOrganizationId == null) {
            return;
        }

        List<Long> recipientIds = userRepository.findActiveIdsByOrganizationId(receiverOrganizationId);
        fanOut(type, event.entityType(), event.entityId(), receiverOrganizationId,
                excludingActor(recipientIds, event.actorUserId()), messageKey,
                new Object[]{event.entityId()});
    }

    private void handleCardAssigned(EntityCreatedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_ASSIGNED_ENABLED, true)) {
            return;
        }
        Card card = cardRepository.findById(event.entityId()).orElse(null);
        if (card == null) {
            log.warn("event=notification_source_not_found entityType=CARD entityId={}", event.entityId());
            return;
        }

        Long organizationId = resolveOrganizationId(card);
        List<Long> recipientIds = card.getUsers().stream().map(User::getId).toList();

        fanOut(NotificationType.CARD_ASSIGNED, AuditEntityType.CARD, card.getId(), organizationId,
                excludingActor(recipientIds, event.actorUserId()), "notification.card-assigned",
                new Object[]{card.getId()});
    }

    private void handleActAssigned(EntityCreatedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_ACT_ASSIGNED_ENABLED, true)) {
            return;
        }
        Act act = actRepository.findById(event.entityId()).orElse(null);
        if (act == null) {
            log.warn("event=notification_source_not_found entityType=ACT entityId={}", event.entityId());
            return;
        }

        Long organizationId = act.getCard() != null ? resolveOrganizationId(act.getCard()) : null;
        List<Long> recipientIds = act.getUsers().stream().map(User::getId).toList();

        fanOut(NotificationType.ACT_ASSIGNED, AuditEntityType.ACT, act.getId(), organizationId,
                excludingActor(recipientIds, event.actorUserId()), "notification.act-assigned",
                new Object[]{act.getId()});
    }

    private void handleActLisResponse(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_ACT_LIS_RESPONSE_ENABLED, true)) {
            return;
        }
        Act act = actRepository.findById(event.entityId()).orElse(null);
        if (act == null) {
            log.warn("event=notification_source_not_found entityType=ACT entityId={}", event.entityId());
            return;
        }

        Long organizationId = act.getCard() != null ? resolveOrganizationId(act.getCard()) : null;
        List<Long> recipientIds = act.getUsers().stream().map(User::getId).toList();

        fanOut(NotificationType.ACT_LIS_RESPONSE, AuditEntityType.ACT, act.getId(), organizationId,
                excludingActor(recipientIds, event.actorUserId()), "notification.act-lis-response",
                new Object[]{act.getId()});
    }

    private Long resolveOrganizationId(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getReceiverOrganizationId();
        }
        if (card.getForm0581() != null) {
            return card.getForm0581().getReceiverOrganizationId();
        }
        return null;
    }

    private List<Long> excludingActor(List<Long> recipientIds, Long actorUserId) {
        if (actorUserId == null) {
            return recipientIds;
        }
        return recipientIds.stream().filter(id -> !Objects.equals(id, actorUserId)).toList();
    }

    private void fanOut(
            NotificationType type, AuditEntityType entityType, Long entityId, Long organizationId,
            List<Long> recipientIds, String messageKey, Object[] params
    ) {
        Set<Long> distinctRecipients = Set.copyOf(recipientIds);
        if (distinctRecipients.isEmpty()) {
            return;
        }

        Instant occurredAt = Instant.now();
        String paramsJson = writeParams(params);

        List<Notification> rows = distinctRecipients.stream()
                .<Notification>map(recipientId -> Notification.builder()
                        .type(type)
                        .entityType(entityType)
                        .entityId(entityId)
                        .recipientUserId(recipientId)
                        .organizationId(organizationId)
                        .messageKey(messageKey)
                        .messageParams(paramsJson)
                        .read(false)
                        .occurredAt(occurredAt)
                        .build())
                .toList();

        try {
            notificationRepository.saveAll(rows);
        } catch (RuntimeException persistenceFailure) {
            log.error("event=notification_write_failure type={} entityType={} entityId={} failureType={}",
                    type, entityType, entityId, persistenceFailure.getClass().getName(), persistenceFailure);
        }
    }

    private String writeParams(Object[] params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JacksonException serializationFailure) {
            log.warn("event=notification_params_serialization_failure failureType={}",
                    serializationFailure.getClass().getName());
            return null;
        }
    }
}
