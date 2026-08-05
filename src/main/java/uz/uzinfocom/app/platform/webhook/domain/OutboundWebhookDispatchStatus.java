package uz.uzinfocom.app.platform.webhook.domain;

/**
 * Lifecycle of one {@link OutboundWebhookDispatch} row. {@code SENDING} is a
 * transient in-flight marker set just before the HTTP call
 * ({@code OutboundWebhookDispatchSendOrchestrator}) and always replaced by
 * {@code SUCCEEDED}, rescheduled back to {@code PENDING}, or {@code EXHAUSTED}
 * once the call returns — never left standing.
 */
public enum OutboundWebhookDispatchStatus {
    PENDING,
    SENDING,
    SUCCEEDED,
    EXHAUSTED
}
