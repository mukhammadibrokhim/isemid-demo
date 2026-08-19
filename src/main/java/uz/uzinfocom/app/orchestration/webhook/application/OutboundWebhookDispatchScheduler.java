package uz.uzinfocom.app.orchestration.webhook.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Polls due {@code outbound_webhook_dispatch} rows and hands each one to
 * {@link OutboundWebhookDispatchSendOrchestrator} - the only path that
 * actually attempts delivery, for both a dispatch's first attempt and every
 * retry. Each dispatch gets its own {@code try/catch} so one failure never
 * stops the rest of the batch from being processed this poll.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundWebhookDispatchScheduler {

    private final OutboundWebhookDispatchService outboundWebhookDispatchService;
    private final OutboundWebhookDispatchSendOrchestrator outboundWebhookDispatchSendOrchestrator;
    private final RetryPolicyResolver retryPolicyResolver;

    @Scheduled(fixedDelayString = "${app.webhook.dispatch-poll-interval-ms:15000}")
    public void pollAndSend() {
        List<Long> dueDispatchIds = outboundWebhookDispatchService.pollDueDispatchIds(retryPolicyResolver.batchSize());
        if (dueDispatchIds.isEmpty()) {
            return;
        }

        log.debug("event=outbound_webhook_dispatch_poll dueCount={}", dueDispatchIds.size());

        for (Long dispatchId : dueDispatchIds) {
            try {
                outboundWebhookDispatchSendOrchestrator.send(dispatchId);
            } catch (RuntimeException unexpected) {
                log.error("event=outbound_webhook_dispatch_poll_failure id={} failureType={}",
                        dispatchId, unexpected.getClass().getName(), unexpected);
            }
        }
    }
}
