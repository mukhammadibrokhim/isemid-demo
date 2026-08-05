package uz.uzinfocom.app.platform.webhook.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.webhook.domain.OutboundWebhookDispatch;
import uz.uzinfocom.app.platform.webhook.domain.OutboundWebhookDispatchStatus;
import uz.uzinfocom.app.platform.webhook.repository.OutboundWebhookDispatchRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboundWebhookDispatchServiceTest {

    private final OutboundWebhookDispatchRepository outboundWebhookDispatchRepository =
            mock(OutboundWebhookDispatchRepository.class);
    private final RetryPolicyResolver retryPolicyResolver = mock(RetryPolicyResolver.class);

    private final OutboundWebhookDispatchService service =
            new OutboundWebhookDispatchService(outboundWebhookDispatchRepository, retryPolicyResolver);

    @Test
    void enqueuePersistsAPendingDispatchReadyForImmediateDelivery() {
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> {
                    OutboundWebhookDispatch dispatch = invocation.getArgument(0);
                    dispatch.setId(1L);
                    return dispatch;
                });

        OutboundWebhookDispatch saved = service.enqueue(
                10L, AuditEntityType.FORM058, 100L, "SENT", "APPROVED", "{\"a\":1}");

        assertThat(saved.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.PENDING);
        assertThat(saved.getAttemptCount()).isZero();
        assertThat(saved.getIntegrationClientId()).isEqualTo(10L);
        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.FORM058);
        assertThat(saved.getEntityId()).isEqualTo(100L);
        assertThat(saved.getOldStatus()).isEqualTo("SENT");
        assertThat(saved.getNewStatus()).isEqualTo("APPROVED");
        assertThat(saved.getPayload()).isEqualTo("{\"a\":1}");
        assertThat(saved.getNextAttemptAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void markSendingIncrementsAttemptCountAndOnlyTransitionsAPendingDispatch() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OutboundWebhookDispatch sending = service.markSending(1L);

        assertThat(sending.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.SENDING);
        assertThat(sending.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void markSendingRejectsADispatchThatIsNotPending() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        dispatch.setStatus(OutboundWebhookDispatchStatus.SUCCEEDED);
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));

        assertThatThrownBy(() -> service.markSending(1L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recordFailureReschedulesBackToPendingWhileRetriesRemain() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        dispatch.markSending();
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(retryPolicyResolver.nextDelay(1)).thenReturn(Optional.of(Duration.ofMinutes(5)));

        service.recordFailure(1L, "connection refused", null);

        assertThat(dispatch.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.PENDING);
        assertThat(dispatch.getLastError()).isEqualTo("connection refused");
        assertThat(dispatch.getNextAttemptAt()).isAfter(Instant.now().plus(Duration.ofMinutes(4)));
    }

    @Test
    void recordFailureMarksExhaustedOnceRetriesAreUsedUp() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        dispatch.markSending();
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(retryPolicyResolver.nextDelay(1)).thenReturn(Optional.empty());

        service.recordFailure(1L, "still failing", 503);

        assertThat(dispatch.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.EXHAUSTED);
        assertThat(dispatch.getLastHttpStatus()).isEqualTo(503);
    }

    @Test
    void recordSuccessMarksSucceededAndClearsAnyPriorError() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        dispatch.markSending();
        dispatch.setLastError("previous failure");
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.recordSuccess(1L, 200);

        assertThat(dispatch.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.SUCCEEDED);
        assertThat(dispatch.getLastHttpStatus()).isEqualTo(200);
        assertThat(dispatch.getLastError()).isNull();
    }

    @Test
    void retryNowResetsAnExhaustedDispatchBackToPendingWithZeroAttempts() {
        OutboundWebhookDispatch dispatch = pendingDispatch();
        dispatch.setStatus(OutboundWebhookDispatchStatus.EXHAUSTED);
        dispatch.setAttemptCount(5);
        when(outboundWebhookDispatchRepository.findById(1L)).thenReturn(Optional.of(dispatch));
        when(outboundWebhookDispatchRepository.save(any(OutboundWebhookDispatch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.retryNow(1L);

        assertThat(dispatch.getStatus()).isEqualTo(OutboundWebhookDispatchStatus.PENDING);
        assertThat(dispatch.getAttemptCount()).isZero();
        assertThat(dispatch.getNextAttemptAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void operationsOnAMissingDispatchThrowNotFound() {
        when(outboundWebhookDispatchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markSending(99L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.retryNow(99L)).isInstanceOf(NotFoundException.class);
    }

    private OutboundWebhookDispatch pendingDispatch() {
        OutboundWebhookDispatch dispatch = OutboundWebhookDispatch.builder()
                .integrationClientId(10L)
                .entityType(AuditEntityType.FORM058)
                .entityId(100L)
                .oldStatus("SENT")
                .newStatus("APPROVED")
                .payload("{}")
                .status(OutboundWebhookDispatchStatus.PENDING)
                .attemptCount(0)
                .nextAttemptAt(Instant.now())
                .build();
        dispatch.setId(1L);
        return dispatch;
    }
}
