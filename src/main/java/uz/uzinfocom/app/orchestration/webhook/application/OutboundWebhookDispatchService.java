package uz.uzinfocom.app.orchestration.webhook.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatch;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatchStatus;
import uz.uzinfocom.app.orchestration.webhook.repository.OutboundWebhookDispatchRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Owns every state transition of an {@link OutboundWebhookDispatch} row.
 * Deliberately the only writer of dispatch state - both
 * {@code OutboundWebhookEventListener} (enqueue) and
 * {@code OutboundWebhookDispatchSendOrchestrator} (markSending/recordSuccess/
 * recordFailure) go through this bean rather than touching the repository
 * directly, so the state machine stays in one place.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundWebhookDispatchService {

    private final OutboundWebhookDispatchRepository outboundWebhookDispatchRepository;
    private final RetryPolicyResolver retryPolicyResolver;

    @Transactional
    public OutboundWebhookDispatch enqueue(
            Long integrationClientId,
            AuditEntityType entityType,
            Long entityId,
            String oldStatus,
            String newStatus,
            String payload
    ) {
        OutboundWebhookDispatch dispatch = OutboundWebhookDispatch.builder()
                .integrationClientId(integrationClientId)
                .entityType(entityType)
                .entityId(entityId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .payload(payload)
                .status(OutboundWebhookDispatchStatus.PENDING)
                .attemptCount(0)
                .nextAttemptAt(Instant.now())
                .build();

        OutboundWebhookDispatch saved = outboundWebhookDispatchRepository.save(dispatch);
        log.info("event=outbound_webhook_dispatch_enqueued id={} integrationClientId={} entityType={} entityId={} "
                        + "oldStatus={} newStatus={}",
                saved.getId(), integrationClientId, entityType, entityId, oldStatus, newStatus);
        return saved;
    }

    /**
     * Runs the {@code FOR UPDATE SKIP LOCKED} poll inside a real (non
     * read-only) transaction - see
     * {@link OutboundWebhookDispatchRepository#findDueForDispatch}. Returns
     * ids rather than entities: by the time the caller processes them, this
     * transaction has already committed and the entities are detached.
     */
    @Transactional
    public List<Long> pollDueDispatchIds(int batchSize) {
        return outboundWebhookDispatchRepository.findDueForDispatch(Instant.now(), batchSize).stream()
                .map(OutboundWebhookDispatch::getId)
                .toList();
    }

    /**
     * Transaction 1 of the send orchestrator's three - see
     * {@code OutboundWebhookDispatchSendOrchestrator}. Only transitions a
     * dispatch still {@code PENDING}: the final safety net against a
     * duplicate send if two pollers ever raced past
     * {@link #pollDueDispatchIds} onto the same row (see that method's
     * javadoc).
     */
    @Transactional
    public OutboundWebhookDispatch markSending(Long id) {
        OutboundWebhookDispatch dispatch = getRequired(id);
        if (dispatch.getStatus() != OutboundWebhookDispatchStatus.PENDING) {
            throw new IllegalStateException(
                    "Outbound webhook dispatch " + id + " is not PENDING (status=" + dispatch.getStatus() + ")");
        }
        dispatch.markSending();
        return outboundWebhookDispatchRepository.save(dispatch);
    }

    @Transactional
    public void recordSuccess(Long id, int httpStatus) {
        OutboundWebhookDispatch dispatch = getRequired(id);
        dispatch.recordSuccess(httpStatus);
        outboundWebhookDispatchRepository.save(dispatch);
        log.info("event=outbound_webhook_dispatch_succeeded id={} httpStatus={}", id, httpStatus);
    }

    @Transactional
    public void recordFailure(Long id, String error, Integer httpStatus) {
        OutboundWebhookDispatch dispatch = getRequired(id);
        Optional<Duration> nextDelay = retryPolicyResolver.nextDelay(dispatch.getAttemptCount());

        if (nextDelay.isPresent()) {
            dispatch.recordFailureAndReschedule(error, httpStatus, Instant.now().plus(nextDelay.get()));
            outboundWebhookDispatchRepository.save(dispatch);
            log.warn("event=outbound_webhook_dispatch_retry_scheduled id={} attemptCount={} httpStatus={} "
                            + "nextAttemptAt={}",
                    id, dispatch.getAttemptCount(), httpStatus, dispatch.getNextAttemptAt());
        } else {
            dispatch.markExhausted(error, httpStatus);
            outboundWebhookDispatchRepository.save(dispatch);
            log.warn("event=outbound_webhook_dispatch_exhausted id={} attemptCount={} httpStatus={}",
                    id, dispatch.getAttemptCount(), httpStatus);
        }
    }

    @Transactional
    public void retryNow(Long id) {
        OutboundWebhookDispatch dispatch = getRequired(id);
        dispatch.retryNow();
        outboundWebhookDispatchRepository.save(dispatch);
        log.info("event=outbound_webhook_dispatch_manual_retry id={}", id);
    }

    private OutboundWebhookDispatch getRequired(Long id) {
        return outboundWebhookDispatchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("outbound-webhook-dispatch.not-found", id));
    }
}
