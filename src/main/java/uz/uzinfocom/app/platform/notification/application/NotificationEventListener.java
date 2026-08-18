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
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationJpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.export.domain.event.ExportJobCompletedEvent;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.notification.domain.Notification;
import uz.uzinfocom.app.platform.notification.domain.NotificationType;
import uz.uzinfocom.app.platform.notification.repository.NotificationRepository;
import uz.uzinfocom.app.platform.scope.FormAccessScopeResolver;
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
    private static final String KEY_FORM058_ACKNOWLEDGED_ENABLED = "notification.form058-acknowledged.enabled";
    private static final String KEY_FORM058_CARD_LINKED_ENABLED = "notification.form058-card-linked.enabled";
    private static final String KEY_FORM058_APPROVED_ENABLED = "notification.form058-approved.enabled";
    private static final String KEY_FORM058_CANCELED_ENABLED = "notification.form058-canceled.enabled";
    private static final String KEY_FORM058_REOPENED_ENABLED = "notification.form058-reopened.enabled";
    private static final String KEY_FORM058_AFFILIATED_RECEIVED_ENABLED = "notification.form058-affiliated-received.enabled";
    private static final String KEY_FORM058_AFFILIATED_CARD_LINKED_ENABLED = "notification.form058-affiliated-card-linked.enabled";
    private static final String KEY_FORM0581_RECEIVED_ENABLED = "notification.form0581-received.enabled";
    private static final String KEY_FORM0581_ACKNOWLEDGED_ENABLED = "notification.form0581-acknowledged.enabled";
    private static final String KEY_FORM0581_CARD_LINKED_ENABLED = "notification.form0581-card-linked.enabled";
    private static final String KEY_FORM0581_APPROVED_ENABLED = "notification.form0581-approved.enabled";
    private static final String KEY_FORM0581_CANCELED_ENABLED = "notification.form0581-canceled.enabled";
    private static final String KEY_FORM0581_REOPENED_ENABLED = "notification.form0581-reopened.enabled";
    private static final String KEY_CARD_ASSIGNED_ENABLED = "notification.card-assigned.enabled";
    private static final String KEY_CARD_ACCEPTED_BY_USER_ENABLED = "notification.card-accepted-by-user.enabled";
    private static final String KEY_CARD_REJECTED_BY_USER_ENABLED = "notification.card-rejected-by-user.enabled";
    private static final String KEY_CARD_COMPLETED_ENABLED = "notification.card-completed.enabled";
    private static final String KEY_CARD_APPROVED_ENABLED = "notification.card-approved.enabled";
    private static final String KEY_CARD_REJECTED_ENABLED = "notification.card-rejected.enabled";
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
    private final PatientAffiliationJpaRepository patientAffiliationRepository;

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
        switch (event.entityType()) {
            case ACT -> handleActStatusChanged(event);
            case CARD -> handleCardStatusChanged(event);
            case FORM058 -> handleForm058StatusChanged(event);
            case FORM0581 -> handleForm0581StatusChanged(event);
            default -> { }
        }
    }

    private void handleActStatusChanged(StatusChangedEvent event) {
        if (!"SENT".equals(event.oldStatus()) || !"COMPLETED".equals(event.newStatus())) {
            return;
        }
        handleActLisResponse(event);
    }

    /**
     * {@code CardStatus.canBeUpdated()}/{@code complete()} let a card reach
     * {@code COMPLETED} from {@code ACCEPTED_BY_USER}, {@code IN_PROGRESS},
     * or {@code REJECTED} (rework), so that leg is matched on the new
     * status alone — mirrors how {@code handleForm058StatusChanged} matches
     * {@code APPROVED}. The other legs are exact old/new pairs, each
     * produced by exactly one {@code CardCommandService} method.
     */
    private void handleCardStatusChanged(StatusChangedEvent event) {
        if ("NEW".equals(event.oldStatus()) && "ACCEPTED_BY_USER".equals(event.newStatus())) {
            handleCardAcceptedByUser(event);
        } else if (("NEW".equals(event.oldStatus()) || "ACCEPTED_BY_USER".equals(event.oldStatus()))
                && "REJECTED_BY_USER".equals(event.newStatus())) {
            handleCardRejectedByUser(event);
        } else if ("COMPLETED".equals(event.newStatus())) {
            handleCardCompleted(event);
        } else if ("COMPLETED".equals(event.oldStatus()) && "APPROVED".equals(event.newStatus())) {
            handleCardApproved(event);
        } else if ("COMPLETED".equals(event.oldStatus()) && "REJECTED".equals(event.newStatus())) {
            handleCardRejectedBySupervisor(event);
        }
    }

    /**
     * {@code FormStatus}/{@code Form0581Status} never have a "RECEIVED" value — the receiver's
     * acceptance moves the form to {@code ACCEPTED} (see {@code AcceptForm058Service}/
     * {@code AcceptForm0581Service}), so that's the transition matched here.
     */
    private void handleForm058StatusChanged(StatusChangedEvent event) {
        if ("SENT".equals(event.oldStatus()) && "ACCEPTED".equals(event.newStatus())) {
            handleForm058Acknowledged(event);
        } else if ("ACCEPTED".equals(event.oldStatus()) && "CARD_LINKED".equals(event.newStatus())) {
            handleForm058CardLinked(event);
        } else if ("APPROVED".equals(event.newStatus())) {
            handleForm058Approved(event);
        } else if ("CANCELED".equals(event.newStatus())) {
            handleForm058Canceled(event);
        } else if ("CANCELED".equals(event.oldStatus()) && "SENT".equals(event.newStatus())) {
            handleForm058Reopened(event);
        }
    }

    private void handleForm0581StatusChanged(StatusChangedEvent event) {
        if ("SENT".equals(event.oldStatus()) && "ACCEPTED".equals(event.newStatus())) {
            handleForm0581Acknowledged(event);
        } else if ("ACCEPTED".equals(event.oldStatus()) && "CARD_LINKED".equals(event.newStatus())) {
            handleForm0581CardLinked(event);
        } else if ("APPROVED".equals(event.newStatus())) {
            handleForm0581Approved(event);
        } else if ("CANCELED".equals(event.newStatus())) {
            handleForm0581Canceled(event);
        } else if ("CANCELED".equals(event.oldStatus()) && "SENT".equals(event.newStatus())) {
            handleForm0581Reopened(event);
        }
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

    /**
     * Two independent notifications can fire off the same {@code
     * EntityCreatedEvent(FORM058)}: the receiver ({@code FORM058_RECEIVED},
     * unconditionally applicable) and — new — every organization that is
     * neither sender nor receiver but is the patient's workplace/place of
     * study ({@code FORM058_AFFILIATED_RECEIVED}), telling them the form is
     * now visible to them via {@code GET /v1/form-058/affiliated}. Each has
     * its own feature flag; the repository lookup only happens if at least
     * one of the two is enabled.
     */
    private void handleForm058Received(EntityCreatedEvent event) {
        boolean receivedEnabled = systemSettingResolver.resolveBoolean(KEY_FORM058_RECEIVED_ENABLED, true);
        boolean affiliatedEnabled = systemSettingResolver.resolveBoolean(KEY_FORM058_AFFILIATED_RECEIVED_ENABLED, true);
        if (!receivedEnabled && !affiliatedEnabled) {
            return;
        }

        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        if (receivedEnabled) {
            notifyReceiverOrganization(event, NotificationType.FORM058_RECEIVED, "notification.form058-received",
                    form058.getReceiverOrganizationId());
        }
        if (affiliatedEnabled) {
            notifyAffiliatedOrganizations(form058, event.actorUserId(),
                    NotificationType.FORM058_AFFILIATED_RECEIVED, "notification.form058-affiliated-received");
        }
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
        notifyOrganization(event.entityType(), event.entityId(), event.actorUserId(),
                type, messageKey, receiverOrganizationId);
    }

    private void handleForm058Acknowledged(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM058_ACKNOWLEDGED_ENABLED, true)) {
            return;
        }
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_ACKNOWLEDGED, "notification.form058-acknowledged",
                form058.getSenderOrganizationId());
    }

    private void handleForm058Canceled(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM058_CANCELED_ENABLED, true)) {
            return;
        }
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        // Either party can cancel while still SENT (see Form058CancelValidator) — notify both
        // organizations, since the actor's own side is already excluded by notifyOrganization.
        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_CANCELED, "notification.form058-canceled",
                form058.getSenderOrganizationId());
        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_CANCELED, "notification.form058-canceled",
                form058.getReceiverOrganizationId());
    }

    /**
     * Same two-flags-one-lookup shape as {@link #handleForm058Received}: the
     * sender gets {@code FORM058_CARD_LINKED} as before, and — new — every
     * affiliated organization gets {@code FORM058_AFFILIATED_CARD_LINKED},
     * since this is the transition that actually creates the {@code Card}s
     * they're now allowed to attach {@code Act}s to (see {@code
     * CardCommandService.requireForm058Access}/{@code
     * ActCommandService.requireCardAccess}).
     */
    private void handleForm058CardLinked(StatusChangedEvent event) {
        boolean cardLinkedEnabled = systemSettingResolver.resolveBoolean(KEY_FORM058_CARD_LINKED_ENABLED, true);
        boolean affiliatedEnabled = systemSettingResolver.resolveBoolean(KEY_FORM058_AFFILIATED_CARD_LINKED_ENABLED, true);
        if (!cardLinkedEnabled && !affiliatedEnabled) {
            return;
        }

        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        if (cardLinkedEnabled) {
            notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                    NotificationType.FORM058_CARD_LINKED, "notification.form058-card-linked",
                    form058.getSenderOrganizationId());
        }
        if (affiliatedEnabled) {
            notifyAffiliatedOrganizations(form058, event.actorUserId(),
                    NotificationType.FORM058_AFFILIATED_CARD_LINKED, "notification.form058-affiliated-card-linked");
        }
    }

    private void handleForm058Approved(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM058_APPROVED_ENABLED, true)) {
            return;
        }
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_APPROVED, "notification.form058-approved",
                form058.getReceiverOrganizationId());
    }

    private void handleForm058Reopened(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM058_REOPENED_ENABLED, true)) {
            return;
        }
        Form058 form058 = form058Repository.findById(event.entityId()).orElse(null);
        if (form058 == null) {
            log.warn("event=notification_source_not_found entityType=FORM058 entityId={}", event.entityId());
            return;
        }

        // Super-admin-only escape hatch (Form058.reopen) — either side may have been
        // responsible for the original cancellation, so notify both organizations.
        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_REOPENED, "notification.form058-reopened",
                form058.getSenderOrganizationId());
        notifyOrganization(AuditEntityType.FORM058, event.entityId(), event.actorUserId(),
                NotificationType.FORM058_REOPENED, "notification.form058-reopened",
                form058.getReceiverOrganizationId());
    }

    private void handleForm0581Acknowledged(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_ACKNOWLEDGED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }

        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_ACKNOWLEDGED, "notification.form0581-acknowledged",
                form0581.getSenderOrganizationId());
    }

    private void handleForm0581Canceled(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_CANCELED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }

        // Either party can cancel while still SENT (see Form0581CancelValidator) — notify both
        // organizations, since the actor's own side is already excluded by notifyOrganization.
        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_CANCELED, "notification.form0581-canceled",
                form0581.getSenderOrganizationId());
        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_CANCELED, "notification.form0581-canceled",
                form0581.getReceiverOrganizationId());
    }

    private void handleForm0581CardLinked(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_CARD_LINKED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }

        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_CARD_LINKED, "notification.form0581-card-linked",
                form0581.getSenderOrganizationId());
    }

    private void handleForm0581Approved(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_APPROVED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }

        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_APPROVED, "notification.form0581-approved",
                form0581.getReceiverOrganizationId());
    }

    private void handleForm0581Reopened(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_FORM0581_REOPENED_ENABLED, true)) {
            return;
        }
        Form0581 form0581 = form0581Repository.findById(event.entityId()).orElse(null);
        if (form0581 == null) {
            log.warn("event=notification_source_not_found entityType=FORM0581 entityId={}", event.entityId());
            return;
        }

        // Super-admin-only escape hatch (Form0581.reopen) — either side may have been
        // responsible for the original cancellation, so notify both organizations.
        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_REOPENED, "notification.form0581-reopened",
                form0581.getSenderOrganizationId());
        notifyOrganization(AuditEntityType.FORM0581, event.entityId(), event.actorUserId(),
                NotificationType.FORM0581_REOPENED, "notification.form0581-reopened",
                form0581.getReceiverOrganizationId());
    }

    private void notifyOrganization(
            AuditEntityType entityType, Long entityId, Long actorUserId,
            NotificationType type, String messageKey, Long organizationId
    ) {
        if (organizationId == null) {
            return;
        }

        List<Long> recipientIds = userRepository.findActiveIdsByOrganizationId(organizationId);
        fanOut(type, entityType, entityId, organizationId,
                excludingActor(recipientIds, actorUserId), messageKey, new Object[]{entityId});
    }

    /**
     * Notifies every organization affiliated with the form's patient
     * (workplace/place of study) except sender/receiver — those already get
     * their own dedicated notification from the caller, so including them
     * here would double-notify the same organization for the same event.
     */
    private void notifyAffiliatedOrganizations(
            Form058 form058, Long actorUserId, NotificationType type, String messageKey
    ) {
        for (Long affiliatedOrganizationId : resolveAffiliatedOrganizationIds(form058)) {
            notifyOrganization(AuditEntityType.FORM058, form058.getId(), actorUserId,
                    type, messageKey, affiliatedOrganizationId);
        }
    }

    private List<Long> resolveAffiliatedOrganizationIds(Form058 form058) {
        if (form058.getPatient() == null) {
            return List.of();
        }

        List<Long> affiliatedOrganizationIds = patientAffiliationRepository.findDistinctOrganizationIdsByPatientIdAndTypeIn(
                form058.getPatient().getId(), FormAccessScopeResolver.AFFILIATION_TYPES
        );

        return affiliatedOrganizationIds.stream()
                .filter(organizationId -> !organizationId.equals(form058.getSenderOrganizationId())
                        && !organizationId.equals(form058.getReceiverOrganizationId()))
                .toList();
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

    private void handleCardAcceptedByUser(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_ACCEPTED_BY_USER_ENABLED, true)) {
            return;
        }
        notifyCardAssigner(event, NotificationType.CARD_ACCEPTED_BY_USER, "notification.card-accepted-by-user");
    }

    private void handleCardRejectedByUser(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_REJECTED_BY_USER_ENABLED, true)) {
            return;
        }
        notifyCardAssigner(event, NotificationType.CARD_REJECTED_BY_USER, "notification.card-rejected-by-user");
    }

    private void handleCardCompleted(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_COMPLETED_ENABLED, true)) {
            return;
        }
        notifyCardAssigner(event, NotificationType.CARD_COMPLETED, "notification.card-completed");
    }

    /**
     * Recipient is the supervisor who assigned the card
     * ({@code Card.assignedById}) — used by the three transitions the
     * attached employee drives (accept, reject, complete).
     */
    private void notifyCardAssigner(StatusChangedEvent event, NotificationType type, String messageKey) {
        Card card = cardRepository.findById(event.entityId()).orElse(null);
        if (card == null) {
            log.warn("event=notification_source_not_found entityType=CARD entityId={}", event.entityId());
            return;
        }
        Long assignedById = card.getAssignedById();
        if (assignedById == null) {
            return;
        }

        Long organizationId = resolveOrganizationId(card);
        fanOut(type, AuditEntityType.CARD, card.getId(), organizationId,
                excludingActor(List.of(assignedById), event.actorUserId()), messageKey,
                new Object[]{card.getId()});
    }

    private void handleCardApproved(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_APPROVED_ENABLED, true)) {
            return;
        }
        notifyCardUsers(event, NotificationType.CARD_APPROVED, "notification.card-approved");
    }

    private void handleCardRejectedBySupervisor(StatusChangedEvent event) {
        if (!systemSettingResolver.resolveBoolean(KEY_CARD_REJECTED_ENABLED, true)) {
            return;
        }
        notifyCardUsers(event, NotificationType.CARD_REJECTED, "notification.card-rejected");
    }

    /**
     * Recipients are the card's attached employees ({@code Card.users}) —
     * used by the two transitions the supervisor drives (approve, reject
     * back for rework).
     */
    private void notifyCardUsers(StatusChangedEvent event, NotificationType type, String messageKey) {
        Card card = cardRepository.findById(event.entityId()).orElse(null);
        if (card == null) {
            log.warn("event=notification_source_not_found entityType=CARD entityId={}", event.entityId());
            return;
        }

        Long organizationId = resolveOrganizationId(card);
        List<Long> recipientIds = card.getUsers().stream().map(User::getId).toList();
        fanOut(type, AuditEntityType.CARD, card.getId(), organizationId,
                excludingActor(recipientIds, event.actorUserId()), messageKey, new Object[]{card.getId()});
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
