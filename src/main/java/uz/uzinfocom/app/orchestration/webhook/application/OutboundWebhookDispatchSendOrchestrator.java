package uz.uzinfocom.app.orchestration.webhook.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.orchestration.webhook.crypto.WebhookSecretCipher;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundWebhookAuthType;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.orchestration.webhook.client.OutboundWebhookClient;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatch;

/**
 * Orchestrates one send (first attempt or retry, indistinguishable) across
 * three separate {@link OutboundWebhookDispatchService} transactions with the
 * actual HTTP call happening <em>between</em> them, never inside one - same
 * split {@code ActLisSendService} uses for LIS:
 *
 * <ol>
 *     <li>{@link OutboundWebhookDispatchService#markSending} - commits the
 *     dispatch to {@code SENDING} and returns it, guarding against a
 *     duplicate claim by a racing poller;</li>
 *     <li>the HTTP call itself, through {@link OutboundWebhookClient} - not
 *     transactional, so a slow or hanging partner endpoint never holds a
 *     database connection or row lock;</li>
 *     <li>{@link OutboundWebhookDispatchService#recordSuccess} or
 *     {@link OutboundWebhookDispatchService#recordFailure}, depending on how
 *     step 2 went.</li>
 * </ol>
 *
 * <p>Deliberately not {@code @Transactional} itself, for the same reason as
 * {@code ActLisSendService}. The outer catch is intentionally broad -
 * {@code OutboundWebhookDispatchScheduler} calls this once per due dispatch
 * in a loop, and one bad row (missing client, malformed URL, transport
 * failure, circuit breaker open) must never abort the rest of the batch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundWebhookDispatchSendOrchestrator {

    private final OutboundWebhookDispatchService outboundWebhookDispatchService;
    private final IntegrationClientRepository integrationClientRepository;
    private final WebhookSecretCipher webhookSecretCipher;
    private final OutboundWebhookClient outboundWebhookClient;

    public void send(Long dispatchId) {
        OutboundWebhookDispatch dispatch;
        try {
            dispatch = outboundWebhookDispatchService.markSending(dispatchId);
        } catch (RuntimeException notClaimable) {
            log.debug("event=outbound_webhook_dispatch_skip id={} reason={}", dispatchId, notClaimable.getMessage());
            return;
        }

        try {
            IntegrationClient client = integrationClientRepository.findById(dispatch.getIntegrationClientId())
                    .orElse(null);

            if (!isSendable(client)) {
                outboundWebhookDispatchService.recordFailure(
                        dispatchId, "Webhook target is missing, inactive, or misconfigured", null);
                return;
            }

            String decryptedSecret = client.getWebhookAuthType() == OutboundWebhookAuthType.NONE
                    ? null
                    : webhookSecretCipher.decrypt(client.getWebhookAuthSecretEncrypted());

            int httpStatus = outboundWebhookClient.send(
                    client.getId(),
                    client.getWebhookCallbackUrl(),
                    client.getWebhookHttpMethod(),
                    client.getWebhookAuthType(),
                    client.getWebhookAuthUsername(),
                    client.getWebhookAuthHeaderName(),
                    decryptedSecret,
                    dispatch.getPayload()
            );

            if (httpStatus >= 200 && httpStatus < 300) {
                outboundWebhookDispatchService.recordSuccess(dispatchId, httpStatus);
            } else {
                outboundWebhookDispatchService.recordFailure(
                        dispatchId, "Unexpected HTTP status " + httpStatus, httpStatus);
            }
        } catch (RuntimeException failure) {
            log.warn("event=outbound_webhook_dispatch_send_failure id={} failureType={} message={}",
                    dispatchId, failure.getClass().getName(), failure.getMessage());
            outboundWebhookDispatchService.recordFailure(dispatchId, failure.getMessage(), null);
        }
    }

    private boolean isSendable(IntegrationClient client) {
        return client != null
                && client.isActive()
                && client.isWebhookActive()
                && StringUtils.hasText(client.getWebhookCallbackUrl())
                && client.getWebhookHttpMethod() != null;
    }
}
